package net.litetex.capes.texturecache;

import static net.litetex.capes.util.collections.AdvancedCollectors.toLinkedHashMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.litetex.capes.util.json.JSONSerializer;


@SuppressWarnings("PMD.GodClass") // Use IDE regions for easier navigation
public class TextureCache
{
	private static final Logger LOG = LoggerFactory.getLogger(TextureCache.class);
	
	private static final float TARGET_CACHE_SIZE_PERCENT = 0.8f;
	private static final Duration CLEANUP_INTERVAL = Duration.ofHours(12);
	private static final Duration SAME_HASH_UPDATE_SUPPRESSION_DURATION = Duration.ofHours(1);
	
	private final Duration maxUnusedDuration;
	private final int maxTargetedCacheSize;
	private final int targetedCacheSize;
	
	private final Path baseDir;
	private final Path texturesDir;
	private final Path providerDir;
	private final Path hashesFile;
	
	private final CompletableFuture<Void> initTask;
	
	private Instant nextCleanupExecuteTime = Instant.MIN;
	
	private final Map<String, Map<String, String>> providerHashes =
		Collections.synchronizedMap(new LinkedHashMap<>());
	
	// Using an ordered map here that always contains the latest value at the end
	// This way cleanups can be A LOT (>20x) faster
	private final SequencedMap<String, Instant> hashLastUsed = new LinkedHashMap<>();
	// For some reason there is no Collections.synchronizedSequenceMap, so this needs to be done manually
	private final Object hashLastUsedLock = new Object();
	
	public TextureCache(
		final Path baseDir,
		final Duration maxUnusedDuration,
		final int maxTargetedCacheSize,
		final List<String> providerIds)
	{
		this.maxUnusedDuration = maxUnusedDuration;
		if(maxTargetedCacheSize <= 0)
		{
			throw new IllegalArgumentException("maxTargetedCacheSize needs to be > 1");
		}
		this.maxTargetedCacheSize = maxTargetedCacheSize;
		this.targetedCacheSize = Math.max(Math.round(maxTargetedCacheSize * TARGET_CACHE_SIZE_PERCENT), 1);
		
		if(providerIds.isEmpty())
		{
			throw new IllegalArgumentException("Expected at least one provider");
		}
		
		this.baseDir = baseDir;
		this.texturesDir = ensureDir(baseDir.resolve("textures"));
		this.providerDir = ensureDir(baseDir.resolve("provider"));
		this.hashesFile = baseDir.resolve("hashes.json");
		
		this.initTask = CompletableFuture.runAsync(() -> this.doAsyncInit(providerIds));
	}
	
	// region Init
	
	private void doAsyncInit(final List<String> providerIds)
	{
		final long startMs = System.currentTimeMillis();
		this.initReadHashes();
		
		CompletableFuture.allOf(providerIds.stream()
				.map(this::registerProvider)
				.toArray(CompletableFuture[]::new))
			.join();
		
		CompletableFuture.runAsync(this::cleanupProviders);
		LOG.debug("Async init took {}ms", System.currentTimeMillis() - startMs);
	}
	
	private void initReadHashes()
	{
		try
		{
			final long startMs = System.currentTimeMillis();
			// Lock not needed here because nobody else is doing stuff during this phase
			this.hashLastUsed.putAll(
				tryRead(this.hashesFile, PersistedHashes.class)
					.map(PersistedHashes::lastUsed)
					.orElseGet(HashMap::new)
					.entrySet()
					.stream()
					.collect(toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue)));
			LOG.debug("Init read hashes took {}ms", System.currentTimeMillis() - startMs);
			
			if(this.cleanUpHashesIfRequired())
			{
				this.saveIdHashesAsync();
			}
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to read hashes initially", ex);
		}
	}
	
	private void removeAllLastUsedHashesWithoutLock(final Stream<Map.Entry<String, Instant>> hashStream)
	{
		hashStream.map(Map.Entry::getKey)
			.toList() // Collect to prevent modification of underlying map
			.forEach(this::removeLastUsedWithoutLock);
	}
	
	private void removeLastUsedWithoutLock(final String hash)
	{
		this.hashLastUsed.remove(hash);
		tryDelete(this.resolveForHash(hash));
	}
	
	private CompletableFuture<Void> registerProvider(final String providerId)
	{
		return CompletableFuture.runAsync(() -> {
			final long startMs = System.currentTimeMillis();
			final Map<String, String> validIdHashes =
				tryRead(this.resolveForProviderId(providerId), PersistedProvider.class)
					.map(PersistedProvider::idHashes)
					.orElseGet(HashMap::new)
					.entrySet()
					.stream()
					// Filter out unknown texture hashes
					.filter(e -> {
						synchronized(this.hashLastUsedLock)
						{
							return this.hashLastUsed.containsKey(e.getValue());
						}
					})
					.collect(toLinkedHashMap(Map.Entry::getKey, Map.Entry::getValue));
			
			this.providerHashes.put(
				providerId,
				Collections.synchronizedMap(validIdHashes)
			);
			
			LOG.debug(
				"Registering and loading provider {} took {}ms",
				providerId,
				System.currentTimeMillis() - startMs);
		});
	}
	
	private void cleanupProviders()
	{
		try
		{
			final long startMs = System.currentTimeMillis();
			
			final Path providersIndexFile = this.baseDir.resolve("providers.json");
			
			final Map<String, Instant> providerRegistered =
				tryRead(providersIndexFile, PersistedProvidersIndex.class)
					.map(PersistedProvidersIndex::providerRegistered)
					.orElseGet(LinkedHashMap::new);
			
			final Instant now = Instant.now();
			this.providerHashes.keySet().forEach(p -> providerRegistered.put(p, now));
			
			final Instant mustBeAfter = now.minus(this.maxUnusedDuration);
			providerRegistered.entrySet()
				.stream()
				.filter(e -> !e.getValue().isAfter(mustBeAfter))
				.map(Map.Entry::getKey)
				.toList() // Collect
				.forEach(providerId -> {
					providerRegistered.remove(providerId);
					tryDelete(this.resolveForProviderId(providerId));
				});
			
			trySave(providersIndexFile, new PersistedProvidersIndex(providerRegistered));
			
			LOG.debug("Provider cleanup took {}ms", System.currentTimeMillis() - startMs);
		}
		catch(final Exception ex)
		{
			LOG.error("Failed to cleanup providers", ex);
		}
	}
	
	// endregion
	
	private boolean cleanUpHashesIfRequired()
	{
		final Instant now = Instant.now();
		if(!(this.nextCleanupExecuteTime.isBefore(now)
			|| this.hashLastUsed.size() > this.maxTargetedCacheSize))
		{
			return false;
		}
		
		LOG.debug("Executing cleanup");
		this.nextCleanupExecuteTime = now.plus(CLEANUP_INTERVAL);
		
		boolean requiresSaving;
		final Instant deleteBefore = now.minus(this.maxUnusedDuration);
		
		final long startMs = System.currentTimeMillis();
		synchronized(this.hashLastUsedLock)
		{
			final List<Map.Entry<String, Instant>> entriesToDelete = new ArrayList<>();
			for(final Map.Entry<String, Instant> entry : this.hashLastUsed.entrySet())
			{
				// As the map is ordered: Abort everything else after the first entry that is valid
				if(!entry.getValue().isBefore(deleteBefore))
				{
					break;
				}
				entriesToDelete.add(entry);
			}
			
			this.removeAllLastUsedHashesWithoutLock(entriesToDelete.stream());
			requiresSaving = !entriesToDelete.isEmpty();
		}
		
		final long start2Ms = System.currentTimeMillis();
		LOG.debug("Cleanup with isBefore took {}ms", start2Ms - startMs);
		
		if(this.hashLastUsed.size() > this.maxTargetedCacheSize)
		{
			synchronized(this.hashLastUsedLock)
			{
				this.removeAllLastUsedHashesWithoutLock(this.hashLastUsed.entrySet()
					.stream()
					.limit(Math.max(this.hashLastUsed.size() - this.targetedCacheSize, 0)));
			}
			
			requiresSaving = true;
			LOG.debug("Cleanup trim to targetedCacheSize took {}ms", System.currentTimeMillis() - start2Ms);
		}
		
		return requiresSaving;
	}
	
	private Map<String, String> idHashesForProvider(final String providerId)
	{
		return this.providerHashes.computeIfAbsent(
			providerId,
			ignored -> Collections.synchronizedMap(new LinkedHashMap<>()));
	}
	
	private void saveProviderHashesForAsync(final String providerId, final Map<String, String> idHashes)
	{
		CompletableFuture.runAsync(() -> this.saveProviderHashesFor(providerId, idHashes));
	}
	
	private void saveProviderHashesFor(final String providerId, final Map<String, String> idHashes)
	{
		trySave(this.resolveForProviderId(providerId), new PersistedProvider(idHashes));
	}
	
	private void updateIdHashUseAndSaveAsync(final String hash)
	{
		final Instant lastUsed;
		synchronized(this.hashLastUsedLock)
		{
			lastUsed = this.hashLastUsed.get(hash);
		}
		final Instant now = Instant.now();
		if(lastUsed != null && lastUsed.isAfter(now.minus(SAME_HASH_UPDATE_SUPPRESSION_DURATION)))
		{
			// Suppress update
			return;
		}
		synchronized(this.hashLastUsedLock)
		{
			this.hashLastUsed.putLast(hash, now);
		}
		this.saveIdHashesAsync();
	}
	
	private void saveIdHashesAsync()
	{
		CompletableFuture.runAsync(this::saveIdHashes);
	}
	
	private void saveIdHashes()
	{
		this.cleanUpHashesIfRequired();
		
		final LinkedHashMap<String, Instant> saveMap;
		synchronized(this.hashLastUsedLock)
		{
			saveMap = new LinkedHashMap<>(this.hashLastUsed);
		}
		trySave(this.hashesFile, new PersistedHashes(saveMap));
	}
	
	public Optional<byte[]> loadExistingTexture(final String providerId, final String id)
	{
		this.initTask.join();
		try
		{
			final long startMs = System.currentTimeMillis();
			final Optional<byte[]> optData = this.loadExistingTextureInternal(providerId, id);
			LOG.debug(
				"Loaded texture[providerId={},id={},data.length={}] took {}ms",
				providerId,
				id,
				optData.map(bytes -> bytes.length).orElse(0),
				System.currentTimeMillis() - startMs);
			return optData;
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to load texture", ex);
			return Optional.empty();
		}
	}
	
	private Optional<byte[]> loadExistingTextureInternal(final String providerId, final String id)
	{
		final Map<String, String> idHashes = this.idHashesForProvider(providerId);
		
		final String hash = idHashes.get(id);
		if(hash == null)
		{
			return Optional.empty();
		}
		
		final byte[] loadedData;
		try
		{
			loadedData = Files.readAllBytes(this.resolveForHash(hash));
			if(loadedData.length == 0)
			{
				throw new IOException("Loaded no data");
			}
		}
		catch(final IOException ioe)
		{
			idHashes.remove(hash);
			this.saveProviderHashesForAsync(providerId, idHashes);
			
			return Optional.empty();
		}
		
		this.updateIdHashUseAndSaveAsync(hash);
		
		return Optional.of(loadedData);
	}
	
	public void saveTexture(final String providerId, final String id, final byte[] textureData)
	{
		this.initTask.join();
		try
		{
			final long startMs = System.currentTimeMillis();
			this.saveTextureInternal(providerId, id, textureData);
			LOG.debug(
				"Saving texture[providerId={},id={},data.length={}] took {}ms",
				providerId,
				id,
				textureData.length,
				System.currentTimeMillis() - startMs);
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to save texture", ex);
		}
	}
	
	private void saveTextureInternal(final String providerId, final String id, final byte[] textureData)
	{
		if(textureData.length == 0)
		{
			LOG.warn("Got no texture data to save for providerId={},id={}", providerId, id);
			return;
		}
		
		final String textureSha256 = DigestUtils.sha256Hex(textureData);
		
		final Map<String, String> idHashes = this.idHashesForProvider(providerId);
		if(textureSha256.equals(idHashes.get(id)))
		{
			return;
		}
		
		idHashes.put(id, textureSha256);
		this.saveProviderHashesForAsync(providerId, idHashes);
		
		try
		{
			final Path path = this.resolveForHash(textureSha256);
			Files.createDirectories(path.getParent());
			Files.write(path, textureData);
		}
		catch(final IOException ioe)
		{
			LOG.warn("Failed to save texture for providerId={},id={}", providerId, id, ioe);
			return;
		}
		
		this.updateIdHashUseAndSaveAsync(textureSha256);
	}
	
	private Path resolveForHash(final String hash)
	{
		return this.texturesDir.resolve(hash.substring(0, 2)).resolve(hash);
	}
	
	private Path resolveForProviderId(final String id)
	{
		return this.providerDir.resolve(id + ".json");
	}
	
	// region Serialization and IO
	
	private static Path ensureDir(final Path path)
	{
		if(!Files.exists(path))
		{
			try
			{
				Files.createDirectories(path);
			}
			catch(final IOException ioe)
			{
				throw new UncheckedIOException("Failed to create " + path, ioe);
			}
		}
		return path;
	}
	
	private static <T> Optional<T> tryRead(final Path path, final Class<T> clazz)
	{
		try
		{
			final long startMs = System.currentTimeMillis();
			
			final Optional<T> optContent =
				Optional.ofNullable(JSONSerializer.GSON.fromJson(Files.readString(path), clazz));
			
			LOG.debug("Reading {} took {}ms", path, System.currentTimeMillis() - startMs);
			return optContent;
		}
		catch(final NoSuchFileException nsfe)
		{
			return Optional.empty();
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to read {}", path, ex);
			return Optional.empty();
		}
	}
	
	private static <T> boolean trySave(final Path path, final T value)
	{
		try
		{
			final long startMs = System.currentTimeMillis();
			
			Files.createDirectories(path.getParent());
			Files.writeString(path, JSONSerializer.GSON.toJson(value));
			
			LOG.debug("Saving {} took {}ms", path, System.currentTimeMillis() - startMs);
			return true;
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to save {}", path, ex);
			return false;
		}
	}
	
	private static boolean tryDelete(final Path path)
	{
		try
		{
			return Files.deleteIfExists(path);
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to delete {}", path, ex);
			return false;
		}
	}
	
	// endregion
	
	record PersistedProvidersIndex(
		Map<String, Instant> providerRegistered
	)
	{
	}
	
	
	record PersistedProvider(
		Map<String, String> idHashes
	)
	{
	}
	
	
	record PersistedHashes(
		Map<String, Instant> lastUsed
	)
	{
	}
}
