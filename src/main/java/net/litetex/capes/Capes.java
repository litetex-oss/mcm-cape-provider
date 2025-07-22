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

import com.mojang.authlib.GameProfile;

import net.litetex.capes.config.Config;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.handler.PlayerCapeHandlerManager;
import net.litetex.capes.handler.TextureLoadThrottler;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.MinecraftCapeProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;


public class Capes
{
	public static final String MOD_ID = "cape-provider";
	
	public static final Identifier DEFAULT_ELYTRA_IDENTIFIER =
		Identifier.of("textures/entity/equipment/wings/elytra.png");
	
	public static final Predicate<CapeProvider> EXCLUDE_DEFAULT_MINECRAFT_CP =
		cp -> MinecraftCapeProvider.INSTANCE != cp;
	
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
	
	private final PlayerCapeHandlerManager playerCapeHandlerManager;
	private final TextureLoadThrottler textureLoadThrottler;
	private boolean shouldRefresh;
	
	public Capes(
		final Config config,
		final Consumer<Config> saveConfigFunc,
		final Map<String, CapeProvider> allProviders)
	{
		this.config = config;
		this.saveConfigFunc = saveConfigFunc;
		this.allProviders = allProviders;
		
		this.validateProfile = !Boolean.FALSE.equals(this.config().isValidateProfile()); // Default -> true
		this.loadThrottleSuppressDuration = Optional.ofNullable(this.config().getLoadThrottleSuppressSec())
			.map(Duration::ofSeconds)
			.orElse(Duration.ofMinutes(3));
		this.blockedProviderCapeHashes = Optional.ofNullable(this.config().getBlockedProviderCapeHashes())
			.map(map -> map.entrySet()
				.stream()
				.filter(e -> allProviders.containsKey(e.getKey()))
				.collect(Collectors.toMap(e -> allProviders.get(e.getKey()), Map.Entry::getValue)))
			.orElseGet(Map::of);
		
		this.playerCapeHandlerManager = new PlayerCapeHandlerManager(this);
		this.textureLoadThrottler = new TextureLoadThrottler(this.playerCapeHandlerManager);
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
		this.textureLoadThrottler.clearCache();
		this.playerCapeHandlerManager.clearCache();
		
		final ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
		if(networkHandler != null)
		{
			networkHandler.getPlayerList().forEach(e ->
				this.textureLoadThrottler.loadIfRequired(e.getProfile()));
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
	
	public TextureLoadThrottler textureLoadThrottler()
	{
		return this.textureLoadThrottler;
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
		return false;
	}
}
