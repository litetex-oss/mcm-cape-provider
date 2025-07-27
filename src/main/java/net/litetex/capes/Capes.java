package net.litetex.capes;

import java.time.Duration;
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
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.handler.PlayerCapeHandlerManager;
import net.litetex.capes.handler.ProfileTextureLoadThrottler;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.DefaultMinecraftCapeProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;


public class Capes
{
	private static final Logger LOG = LoggerFactory.getLogger(Capes.class);
	
	public static final String MOD_ID = "cape-provider";
	
	public static final Identifier DEFAULT_ELYTRA_IDENTIFIER =
		Identifier.of("textures/entity/equipment/wings/elytra.png");
	
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
	
	private final boolean validateProfile;
	private final Duration loadThrottleSuppressDuration;
	private final Map<CapeProvider, Set<Integer>> blockedProviderCapeHashes;
	private final int playerCacheSize;
	private final boolean useRealPlayerOnlineValidation;
	
	private final PlayerCapeHandlerManager playerCapeHandlerManager;
	private final ProfileTextureLoadThrottler profileTextureLoadThrottler;
	private boolean shouldRefresh;
	
	@SuppressWarnings("checkstyle:MagicNumber")
	public Capes(
		final Config config,
		final Consumer<Config> saveConfigFunc,
		final Map<String, CapeProvider> allProviders)
	{
		this.config = config;
		this.saveConfigFunc = saveConfigFunc;
		this.allProviders = allProviders;
		
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
		
		final ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
		if(networkHandler != null)
		{
			networkHandler.getPlayerList().forEach(e ->
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
	
	public boolean overwriteSkinTextures(
		final GameProfile profile,
		final Supplier<SkinTextures> oldTexureSupplier,
		final Consumer<SkinTextures> applyOverwrittenTextures)
	{
		final PlayerCapeHandler handler = this.playerCapeHandlerManager().getProfile(profile);
		if(handler != null)
		{
			final Identifier capeTexture = handler.getCape();
			if(capeTexture != null)
			{
				final SkinTextures oldTextures = oldTexureSupplier.get();
				final Identifier elytraTexture = handler.hasElytraTexture()
					&& this.config().isEnableElytraTexture()
					? capeTexture
					: Capes.DEFAULT_ELYTRA_IDENTIFIER;
				applyOverwrittenTextures.accept(new SkinTextures(
					oldTextures.texture(),
					oldTextures.textureUrl(),
					capeTexture,
					elytraTexture,
					oldTextures.model(),
					oldTextures.secure()));
				return true;
			}
		}
		if(!this.isUseDefaultProvider())
		{
			final SkinTextures oldTextures = oldTexureSupplier.get();
			applyOverwrittenTextures.accept(new SkinTextures(
				oldTextures.texture(),
				oldTextures.textureUrl(),
				null,
				null,
				oldTextures.model(),
				oldTextures.secure()));
			return true;
		}
		return false;
	}
}
