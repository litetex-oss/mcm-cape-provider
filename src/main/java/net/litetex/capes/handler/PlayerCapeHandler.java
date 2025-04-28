package net.litetex.capes.handler;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.ResolvedTextureInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;


@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerCapeHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(PlayerCapeHandler.class);
	
	private static final Map<UUID, PlayerCapeHandler> INSTANCES = Collections.synchronizedMap(new HashMap<>());
	
	private static final ExecutorService EXECUTORS = Executors.newFixedThreadPool(
		2,
		new ThreadFactory()
		{
			private static final AtomicInteger COUNTER = new AtomicInteger(0);
			
			@Override
			public Thread newThread(@NotNull final Runnable r)
			{
				final Thread thread = new Thread(r);
				thread.setName("Cape-" + COUNTER.getAndIncrement());
				thread.setDaemon(true);
				return thread;
			}
		});
	
	private final GameProfile profile;
	private int lastFrame;
	private int maxFrames;
	private long lastFrameTime;
	private boolean hasCape;
	private boolean hasElytraTexture = true;
	private boolean hasAnimatedCape;
	
	public PlayerCapeHandler(final GameProfile profile)
	{
		this.profile = profile;
	}
	
	public Identifier getCape()
	{
		if(!this.hasAnimatedCape)
		{
			return identifier(this.uuid().toString());
		}
		
		final long time = System.currentTimeMillis();
		if(time > this.lastFrameTime + 100L)
		{
			final int thisFrame = (this.lastFrame + 1) % this.maxFrames;
			this.lastFrame = thisFrame;
			this.lastFrameTime = time;
			return identifier(this.uuid() + "/" + thisFrame);
		}
		return identifier(this.uuid() + "/" + this.lastFrame);
	}
	
	public void resetCape()
	{
		this.hasCape = false;
		this.hasAnimatedCape = false;
		this.hasElytraTexture = true;
	}
	
	public boolean trySetCape(final CapeProvider capeProvider)
	{
		final String url = capeProvider.getBaseUrl(this.profile);
		if(url == null)
		{
			return false;
		}
		
		try
		{
			final HttpClient.Builder clientBuilder = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(10));
			final Proxy proxy = MinecraftClient.getInstance().getNetworkProxy();
			if(proxy != null)
			{
				clientBuilder.proxy(new ProxySelector()
				{
					@Override
					public List<Proxy> select(final URI uri)
					{
						return List.of(proxy);
					}
					
					@Override
					public void connectFailed(final URI uri, final SocketAddress sa, final IOException ioe)
					{
						// Ignore
					}
				});
			}
			
			final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
				.timeout(Duration.ofSeconds(10))
				.header("User-Agent", "CP");
			
			final ResolvedTextureInfo resolvedTextureInfo =
				capeProvider.resolveTexture(clientBuilder, requestBuilder, this.profile);
			if(resolvedTextureInfo == null || resolvedTextureInfo.imageBytes() == null)
			{
				return false;
			}
			
			final NativeImage cape = NativeImage.read(resolvedTextureInfo.imageBytes());
			this.hasAnimatedCape = resolvedTextureInfo.animated();
			
			// Do texturing work NOT on Render thread
			final Map<Identifier, NativeImage> texturesToRegister;
			if(resolvedTextureInfo.animated())
			{
				texturesToRegister = this.toAnimatedCapeTextureFrames(cape).entrySet()
					.stream()
					.collect(Collectors.toMap(e -> identifier(this.uuid() + "/" + e.getKey()), Map.Entry::getValue));
				
				// Assume that elytra texture is available
				this.hasElytraTexture = true;
				
				this.maxFrames = texturesToRegister.size();
			}
			else
			{
				this.hasElytraTexture = Math.floorDiv(cape.getWidth(), cape.getHeight()) == 2;
				texturesToRegister = Map.of(identifier(this.uuid().toString()), this.toCapeTexture(cape));
			}
			
			final TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
			CompletableFuture.runAsync(
				() -> texturesToRegister.forEach((id, texture) ->
					textureManager.registerTexture(
						id,
						new NativeImageBackedTexture(id::toString, texture))),
				MinecraftClient.getInstance());
			
			this.hasCape = true;
			return true;
		}
		catch(final InterruptedException iex)
		{
			LOG.warn("Got interrupted[url='{}',profileId='{}']", url, this.profile.getId(), iex);
			Thread.currentThread().interrupt();
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to process texture[url='{}',profileId='{}']", url, this.profile.getId(), ex);
		}
		
		this.resetCape();
		return false;
	}
	
	private NativeImage toCapeTexture(final NativeImage img)
	{
		int imageWidth = 64;
		int imageHeight = 32;
		final int srcWidth = img.getWidth();
		final int srcHeight = img.getHeight();
		while(imageWidth < srcWidth || imageHeight < srcHeight)
		{
			imageWidth *= 2;
			imageHeight *= 2;
		}
		final NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
		for(int x = 0; x < srcWidth; x++)
		{
			for(int y = 0; y < srcHeight; y++)
			{
				imgNew.setColorArgb(x, y, img.getColorArgb(x, y));
			}
		}
		img.close();
		return imgNew;
	}
	
	private Map<Integer, NativeImage> toAnimatedCapeTextureFrames(final NativeImage img)
	{
		final Map<Integer, NativeImage> frames = new HashMap<>();
		final int totalFrames = img.getHeight() / (img.getWidth() / 2);
		for(int currentFrame = 0; currentFrame < totalFrames; currentFrame++)
		{
			final NativeImage frame = new NativeImage(img.getWidth(), img.getWidth() / 2, true);
			for(int x = 0; x < frame.getWidth(); x++)
			{
				for(int y = 0; y < frame.getHeight(); y++)
				{
					frame.setColorArgb(x, y, img.getColorArgb(x, y + (currentFrame * (img.getWidth() / 2))));
				}
			}
			frames.put(currentFrame, frame);
		}
		return frames;
	}
	
	public static PlayerCapeHandler getProfile(final GameProfile profile)
	{
		return INSTANCES.get(profile.getId());
	}
	
	// Only use this when required to keep RAM consumption low!
	public static PlayerCapeHandler getOrCreateProfile(final GameProfile profile)
	{
		return INSTANCES.computeIfAbsent(profile.getId(), ignored -> new PlayerCapeHandler(profile));
	}
	
	public static void onLoadTexture(final GameProfile profile)
	{
		final Capes capes = Capes.instance();
		onLoadTexture(
			profile,
			capes.validateProfile(),
			capes.activeCapeProviders(),
			null);
	}
	
	public static void onLoadTexture(
		final GameProfile profile,
		final boolean validateProfile,
		final Collection<CapeProvider> capeProviders,
		final Runnable onAfterLoaded)
	{
		if(LOG.isDebugEnabled())
		{
			LOG.debug("onLoadTexture: {}/{} validate={}", profile.getName(), profile.getId(), validateProfile);
		}
		EXECUTORS.submit(() ->
		{
			if(validateProfile && !capeProviders.isEmpty())
			{
				final MinecraftClient client = MinecraftClient.getInstance();
				
				// The current player is always valid
				final boolean real = profile.getId().equals(client.getSession().getUuidOrNull())
					// Shortcut: Check if the name is valid
					|| isValidName(profile.getName())
					// Check if this is a real player (not a fake one create by a server)
					// Use secure = false to utilize cache
					&& client.getSessionService().fetchProfile(profile.getId(), false) != null;
				
				LOG.debug(
					"Determined that {}/{} is {}a real player",
					profile.getName(),
					profile.getId(),
					real ? "" : "NOT ");
				
				if(!real)
				{
					return;
				}
			}
			
			final PlayerCapeHandler handler = getOrCreateProfile(profile);
			handler.resetCape();
			
			final Optional<CapeProvider> optFoundCapeProvider = capeProviders.stream()
				.filter(handler::trySetCape)
				.findFirst();
			
			if(LOG.isDebugEnabled())
			{
				optFoundCapeProvider.ifPresentOrElse(
					cp ->
						LOG.debug("Loaded cape from {} for {}/{}", cp.id(), profile.getName(), profile.getId()),
					() -> LOG.debug("Found no cape for {}/{}", profile.getName(), profile.getId())
				);
			}
			
			if(onAfterLoaded != null)
			{
				onAfterLoaded.run();
			}
		});
	}
	
	private static boolean isValidName(final String playerName)
	{
		final int length = playerName.length();
		if(length < 3 || length > 16)
		{
			return false;
		}
		
		for(int i = 0; i < length; i++)
		{
			final char c = playerName.charAt(i);
			if(!((c >= 'a' && c <= 'z')
				|| (c >= 'A' && c <= 'Z')
				|| (c >= '0' && c <= '9')
				|| c == '_'))
			{
				return false;
			}
		}
		return true;
	}
	
	public static Identifier identifier(final String id)
	{
		return Identifier.of(Capes.MOD_ID, id);
	}
	
	// region Getter
	
	public UUID uuid()
	{
		return this.profile.getId();
	}
	
	public boolean hasCape()
	{
		return this.hasCape;
	}
	
	public boolean hasElytraTexture()
	{
		return this.hasElytraTexture;
	}
	
	// endregion
}
