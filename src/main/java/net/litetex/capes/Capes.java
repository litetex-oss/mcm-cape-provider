package net.litetex.capes;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.litetex.capes.config.Config;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.MinecraftCapeProvider;
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
	}
	
	public void saveConfig()
	{
		this.saveConfigFunc.accept(this.config);
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
}
