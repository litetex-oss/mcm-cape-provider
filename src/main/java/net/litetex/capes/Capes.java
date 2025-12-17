package net.litetex.capes;

import static java.util.Objects.requireNonNullElse;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.config.Config;
import net.litetex.capes.config.ModProviderHandling;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.handler.PlayerCapeHandlerManager;
import net.litetex.capes.handler.ProfileTextureLoadThrottler;
import net.litetex.capes.handler.textures.TextureResolver;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.CustomProvider;
import net.litetex.capes.provider.DefaultMinecraftCapeProvider;
import net.litetex.capes.provider.ModMetadataProvider;
import net.litetex.capes.texturecache.TextureCache;
import net.litetex.capes.util.CapeProviderTextureAsset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerSkin;


public class Capes
{
	private static final Logger LOG = LoggerFactory.getLogger(Capes.class);
	
	public static final String MOD_ID = "cape-provider";
	
	public static final ClientAsset.Texture DEFAULT_ELYTRA_TEXTURE =
		new CapeProviderTextureAsset(ResourceLocation.parse("textures/entity/equipment/wings/elytra.png"));
	
	public static final Predicate<CapeProvider> EXCLUDE_DEFAULT_MINECRAFT_CP =
		cp -> DefaultMinecraftCapeProvider.INSTANCE != cp;
	
	private static Capes instance;
	
	public static Capes instance()
	{
		return instance;
	}
	
	public static void setInstance(final Capes instance)
	{
		Capes.instance = instance;
	}
	
	private final Config config;
	private final Consumer<Config> saveConfigFunc;
	private final Map<String, CapeProvider> allProviders;
	private final Map<String, TextureResolver> allTextureResolvers;
	
	private final boolean validateProfile;
	private final Duration loadThrottleSuppressDuration;
	private final Map<CapeProvider, Set<Integer>> blockedProviderCapeHashes;
	private final int playerCacheSize;
	private final boolean useRealPlayerOnlineValidation;
	
	private final PlayerCapeHandlerManager playerCapeHandlerManager;
	private final ProfileTextureLoadThrottler profileTextureLoadThrottler;
	private final TextureCache textureCache;
	private boolean shouldRefresh;
	
	@SuppressWarnings("checkstyle:MagicNumber")
	public Capes(
		final Path modStateDir,
		final Config config,
		final Consumer<Config> saveConfigFunc,
		final Map<String, CapeProvider> allProviders,
		final Map<String, TextureResolver> allTextureResolvers)
	{
		this.config = config;
		this.saveConfigFunc = saveConfigFunc;
		this.allProviders = allProviders;
		this.allTextureResolvers = allTextureResolvers;
		
		if(LOG.isDebugEnabled())
		{
			LOG.debug("Providers: {}", allProviders.keySet());
			LOG.debug("Texture-Resolvers: {}", allTextureResolvers.keySet());
		}
		
		// Calculate advanced/debug values
		
		this.validateProfile = !Boolean.FALSE.equals(this.config().isValidateProfile());
		LOG.debug("validateProfile: {}", this.validateProfile);
		
		this.loadThrottleSuppressDuration = Optional.ofNullable(this.config().getLoadThrottleSuppressSec())
			.map(Duration::ofSeconds)
			.orElse(Duration.ofMinutes(3));
		LOG.debug("loadThrottleSuppressDuration: {}", this.loadThrottleSuppressDuration);
		
		this.blockedProviderCapeHashes = Optional.ofNullable(this.config().getBlockedProviderCapeHashes())
			.map(map -> map.entrySet()
				.stream()
				.filter(e -> allProviders.containsKey(e.getKey()))
				.collect(Collectors.toMap(e -> allProviders.get(e.getKey()), Map.Entry::getValue)))
			.orElseGet(Map::of);
		LOG.debug("blockedProviderCapeHashes: {}x", this.blockedProviderCapeHashes.size());
		
		final Integer configPlayerCacheSize = this.config.getPlayerCacheSize();
		this.playerCacheSize = configPlayerCacheSize != null
			? Math.clamp(configPlayerCacheSize, 1, 100_000)
			: 1000;
		LOG.debug("playerCacheSize: {}", this.playerCacheSize);
		
		this.useRealPlayerOnlineValidation = Boolean.TRUE.equals(config.getUseRealPlayerOnlineValidation());
		LOG.debug("useRealPlayerOnlineValidation: {}", this.useRealPlayerOnlineValidation);
		
		this.playerCapeHandlerManager = new PlayerCapeHandlerManager(this);
		this.profileTextureLoadThrottler = new ProfileTextureLoadThrottler(
			this.playerCapeHandlerManager,
			this.playerCacheSize());
		this.textureCache = Optional.of(allProviders.values()
				.stream()
				.filter(CapeProvider::canUseCache)
				.map(CapeProvider::id)
				.toList())
			.filter(l -> !l.isEmpty())
			.map(providerIds -> new TextureCache(
				modStateDir.resolve("texture-cache"),
				Duration.ofDays(Math.clamp(
					requireNonNullElse(config.getTextureCacheDeleteUnusedDays(), 90),
					1,
					1_000)),
				Math.clamp(
					requireNonNullElse(config.getTextureCacheMaxSize(), 500),
					1,
					10_000),
				providerIds))
			.orElse(null);
		
		final long startMs = System.currentTimeMillis();
		this.postProcessModProviders();
		LOG.debug("Post processing mod providers took {}ms", System.currentTimeMillis() - startMs);
	}
	
	protected void postProcessModProviders()
	{
		final ModProviderHandling modProviderHandling = this.config().getModProviderHandling();
		if(modProviderHandling.activateByDefault())
		{
			// Works like this:
			// Mod is present? -> FirstTimeMissing=Instant.MAX
			// Mod was present during last time? -> FirstTimeMissing=NOW
			// Remove all mods where FirstTimeMissing is too old
			final Set<String> providerIdsLoadedByMods = this.getAllProviders().values()
				.stream()
				.filter(ModMetadataProvider.class::isInstance)
				.map(ModMetadataProvider.class::cast)
				.map(CustomProvider::id)
				.collect(Collectors.toCollection(LinkedHashSet::new));
			
			final Instant nullPlaceholder = Instant.MAX; // GSON doesn't serialize nulls by default
			final Instant now = Instant.now();
			final Instant removeOutdated = now.minus(Duration.ofDays(7));
			final Map<String, Instant> knownProviderIdsFirstTimeMissing =
				Optional.ofNullable(this.config().getKnownModProviderIdsFirstTimeMissing())
					.map(Map::entrySet)
					.stream()
					.flatMap(Collection::stream)
					// Remove outdated
					.filter(e -> nullPlaceholder.equals(e.getValue()) || e.getValue().isAfter(removeOutdated))
					.collect(Collectors.toMap(
						Map.Entry::getKey,
						e -> nullPlaceholder.equals(e.getValue()) ? now : e.getValue()));
			
			final Set<String> activeProviderIds = Objects.requireNonNullElseGet(
				this.config().getActiveProviderIds(),
				LinkedHashSet::new);
			providerIdsLoadedByMods.stream()
				.filter(id -> !knownProviderIdsFirstTimeMissing.containsKey(id))
				.forEach(activeProviderIds::add);
			this.config().setActiveProviderIds(activeProviderIds);
			
			providerIdsLoadedByMods.forEach(id -> knownProviderIdsFirstTimeMissing.put(id, nullPlaceholder));
			
			this.config().setKnownModProviderIdsFirstTimeMissing(knownProviderIdsFirstTimeMissing);
			this.saveConfig();
			
			return;
		}
		
		// Reset all known providers due to privacy reasons
		if(this.config().getKnownModProviderIdsFirstTimeMissing() != null)
		{
			this.config().setKnownModProviderIdsFirstTimeMissing(null);
			this.saveConfig();
		}
	}
	
	public void saveConfig()
	{
		this.saveConfigFunc.accept(this.config);
	}
	
	public void saveConfigAndMarkRefresh()
	{
		this.saveConfig();
		this.shouldRefresh = true;
	}
	
	public void refreshIfMarked()
	{
		if(this.shouldRefresh)
		{
			this.refresh();
			this.shouldRefresh = false;
		}
	}
	
	protected void refresh()
	{
		this.profileTextureLoadThrottler.clearCache();
		this.playerCapeHandlerManager.clearCache();
		
		final ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
		if(networkHandler != null)
		{
			networkHandler.getOnlinePlayers().forEach(e ->
				this.profileTextureLoadThrottler.loadIfRequired(e.getProfile()));
		}
	}
	
	public Config config()
	{
		return this.config;
	}
	
	public Map<String, CapeProvider> getAllProviders()
	{
		return this.allProviders;
	}
	
	public Map<String, TextureResolver> getAllTextureResolvers()
	{
		return this.allTextureResolvers;
	}
	
	public Optional<CapeProvider> getCapeProviderForSelf()
	{
		return Optional.ofNullable(this.config.getCurrentPreviewProviderId())
			.map(this.allProviders::get);
	}
	
	public List<CapeProvider> activeCapeProviders()
	{
		return this.config.getActiveProviderIds().stream()
			.map(this.allProviders::get)
			.filter(EXCLUDE_DEFAULT_MINECRAFT_CP)
			.filter(Objects::nonNull)
			.toList();
	}
	
	public boolean isUseDefaultProvider()
	{
		return this.config().isUseDefaultProvider();
	}
	
	public boolean validateProfile()
	{
		return this.validateProfile;
	}
	
	public Duration loadThrottleSuppressDuration()
	{
		return this.loadThrottleSuppressDuration;
	}
	
	public Map<CapeProvider, Set<Integer>> blockedProviderCapeHashes()
	{
		return this.blockedProviderCapeHashes;
	}
	
	public int playerCacheSize()
	{
		return this.playerCacheSize;
	}
	
	public boolean useRealPlayerOnlineValidation()
	{
		return this.useRealPlayerOnlineValidation;
	}
	
	public ProfileTextureLoadThrottler textureLoadThrottler()
	{
		return this.profileTextureLoadThrottler;
	}
	
	public PlayerCapeHandlerManager playerCapeHandlerManager()
	{
		return this.playerCapeHandlerManager;
	}
	
	public TextureCache textureCache()
	{
		return this.textureCache;
	}
	
	public boolean overwriteSkinTextures(
		final GameProfile profile,
		final Supplier<PlayerSkin> oldTexureSupplier,
		final Consumer<PlayerSkin> applyOverwrittenTextures)
	{
		final PlayerCapeHandler handler = this.playerCapeHandlerManager().getProfile(profile);
		if(handler != null)
		{
			final ClientAsset.Texture capeTexture = handler.getCape();
			if(capeTexture != null)
			{
				final PlayerSkin oldTextures = oldTexureSupplier.get();
				final ClientAsset.Texture elytraTexture = handler.hasElytraTexture()
					&& this.config().isEnableElytraTexture()
					? capeTexture
					: Capes.DEFAULT_ELYTRA_TEXTURE;
				applyOverwrittenTextures.accept(new PlayerSkin(
					oldTextures.body(),
					capeTexture,
					elytraTexture,
					oldTextures.model(),
					oldTextures.secure()));
				return true;
			}
		}
		if(!this.isUseDefaultProvider())
		{
			final PlayerSkin oldTextures = oldTexureSupplier.get();
			applyOverwrittenTextures.accept(new PlayerSkin(
				oldTextures.body(),
				null,
				null,
				oldTextures.model(),
				oldTextures.secure()));
			return true;
		}
		return false;
	}
}
