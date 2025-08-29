package net.litetex.capes.handler;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.config.AnimatedCapesHandling;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.ResolvedTextureInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;


@SuppressWarnings({"checkstyle:MagicNumber", "PMD.GodClass"})
public class PlayerCapeHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(PlayerCapeHandler.class);
	
	private final Capes capes;
	private final GameProfile profile;
	private Optional<IdentifierProvider> optIdentifierProvider = Optional.empty();
	private boolean hasElytraTexture = true;
	
	public PlayerCapeHandler(final Capes capes, final GameProfile profile)
	{
		this.capes = capes;
		this.profile = profile;
	}
	
	public Optional<IdentifierProvider> capeIdentifierProvider()
	{
		return this.optIdentifierProvider;
	}
	
	public Identifier getCape()
	{
		final IdentifierProvider identifierProvider = this.optIdentifierProvider.orElse(null);
		if(identifierProvider != null)
		{
			return identifierProvider.identifier();
		}
		return null;
	}
	
	public void resetCape()
	{
		this.optIdentifierProvider = Optional.empty();
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
			final HttpClient.Builder clientBuilder = this.createBuilder();
			
			final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(url))
				.timeout(Duration.ofSeconds(10))
				.header("User-Agent", "CP");
			
			final ResolvedTextureInfo resolvedTextureInfo =
				capeProvider.resolveTexture(clientBuilder, requestBuilder, this.profile);
			if(resolvedTextureInfo == null || resolvedTextureInfo.imageBytes() == null)
			{
				return false;
			}
			
			if(this.isCapeBlocked(capeProvider, resolvedTextureInfo.imageBytes()))
			{
				return false;
			}
			
			final NativeImage cape = NativeImage.read(resolvedTextureInfo.imageBytes());
			final boolean isAnimatedTexture = resolvedTextureInfo.animated();
			
			if(isAnimatedTexture && this.animatedCapesHandling() == AnimatedCapesHandling.OFF)
			{
				return false;
			}
			
			this.optIdentifierProvider = this.registerTexturesAndGetProvider(
				this.determineTexturesToRegister(isAnimatedTexture, cape, url));
			
			return this.optIdentifierProvider.isPresent();
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
	
	private HttpClient.Builder createBuilder()
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
		return clientBuilder;
	}
	
	private boolean isCapeBlocked(final CapeProvider provider, final byte[] imageBytes)
	{
		final Set<Integer> blockedCapeHashes = this.capes.blockedProviderCapeHashes().get(provider);
		if(blockedCapeHashes == null)
		{
			return false;
		}
		
		return blockedCapeHashes.contains(Arrays.hashCode(imageBytes));
	}
	
	private Map<Identifier, NativeImage> determineTexturesToRegister(
		final boolean isAnimatedTexture,
		final NativeImage cape,
		final String url)
	{
		if(!isAnimatedTexture)
		{
			this.hasElytraTexture = Math.floorDiv(cape.getWidth(), cape.getHeight()) == 2;
			return Map.of(identifier(this.uuid().toString()), this.toCapeTexture(cape));
		}
		
		Stream<Map.Entry<Integer, NativeImage>> animatedTextureStream =
			this.toAnimatedCapeTextureFrames(cape).entrySet().stream();
		
		final boolean freezeAnimatation = this.animatedCapesHandling() == AnimatedCapesHandling.FROZEN;
		if(freezeAnimatation)
		{
			animatedTextureStream = animatedTextureStream.limit(1);
		}
		
		final Map<Identifier, NativeImage> texturesToRegister = animatedTextureStream
			.collect(Collectors.toMap(
				e -> identifier(
					this.uuid() + (!freezeAnimatation ? "/" + e.getKey() : "")),
				Map.Entry::getValue,
				(l, r) -> r,
				LinkedHashMap::new));
		
		if(texturesToRegister.isEmpty())
		{
			LOG.warn(
				"Received animated texture with no frames[url='{}',profileId='{}']",
				url,
				this.profile.getId());
		}
		
		// Assume that elytra texture is available
		this.hasElytraTexture = true;
		return texturesToRegister;
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
	
	private Optional<IdentifierProvider> registerTexturesAndGetProvider(
		final Map<Identifier, NativeImage> texturesToRegister)
	{
		if(texturesToRegister.isEmpty())
		{
			return Optional.empty();
		}
		
		final TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
		// Do texturing work NOT on Render thread
		CompletableFuture.runAsync(
				() -> texturesToRegister.forEach((id, texture) ->
					textureManager.registerTexture(
						id,
						new NativeImageBackedTexture(id::toString, texture))),
				MinecraftClient.getInstance())
			.exceptionally(ex -> {
				LOG.warn("Failed to register textures", ex);
				return null;
			});
		
		return Optional.of(texturesToRegister.size() == 1
			? new DefaultIdentifierProvider(texturesToRegister.keySet().iterator().next())
			: new AnimatedIdentifierProvider(texturesToRegister.keySet()));
	}
	
	private AnimatedCapesHandling animatedCapesHandling()
	{
		return this.capes.config().getAnimatedCapesHandling();
	}
	
	static Identifier identifier(final String id)
	{
		return Identifier.of(Capes.MOD_ID, id);
	}
	
	// region Getter
	
	public UUID uuid()
	{
		return this.profile.getId();
	}
	
	public boolean hasElytraTexture()
	{
		return this.hasElytraTexture;
	}
	
	// endregion
	
	
	record DefaultIdentifierProvider(Identifier identifier) implements IdentifierProvider
	{
		@Override
		public boolean dynamicIdentifier()
		{
			return false;
		}
	}
	
	
	static class AnimatedIdentifierProvider implements IdentifierProvider
	{
		private final List<Identifier> identifiers;
		private int lastFrameIndex;
		private long nextFrameTime;
		
		public AnimatedIdentifierProvider(final Collection<Identifier> identifiers)
		{
			this.identifiers = new ArrayList<>(identifiers);
		}
		
		@Override
		public Identifier identifier()
		{
			final long time = System.currentTimeMillis();
			if(time > this.nextFrameTime)
			{
				final int thisFrameIndex = (this.lastFrameIndex + 1) % this.identifiers.size();
				this.lastFrameIndex = thisFrameIndex;
				this.nextFrameTime = time + 100L;
				
				return this.identifiers.get(thisFrameIndex);
			}
			return this.identifiers.get(this.lastFrameIndex);
		}
		
		@Override
		public boolean dynamicIdentifier()
		{
			return true;
		}
	}
}
