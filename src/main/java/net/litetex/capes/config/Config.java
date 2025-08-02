package net.litetex.capes.config;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.litetex.capes.provider.MinecraftCapesCapeProvider;
import net.litetex.capes.provider.OptiFineCapeProvider;


public class Config
{
	private String currentPreviewProviderId;
	// NOTE: Default/Minecraft is always active
	private Set<String> activeProviderIds;
	private boolean useDefaultProvider = true;
	private boolean onlyLoadForSelf;
	private boolean enableElytraTexture;
	private AnimatedCapesHandling animatedCapesHandling = AnimatedCapesHandling.ON;
	private List<CustomProviderConfig> customProviders = List.of();
	private ModProviderHandling modProviderHandling = ModProviderHandling.ON;
	private Map<String, Instant> knownModProviderIdsFirstTimeMissing;
	
	// Advanced/Debug options
	private Boolean validateProfile;
	private Integer loadThrottleSuppressSec;
	private Map<String, Set<Integer>> blockedProviderCapeHashes;
	private Integer loadThreads;
	private Integer playerCacheSize;
	private Boolean useRealPlayerOnlineValidation;
	
	public void reset()
	{
		this.setCurrentPreviewProviderId(null);
		this.setActiveProviderIds(List.of(MinecraftCapesCapeProvider.ID, OptiFineCapeProvider.ID));
		this.setUseDefaultProvider(true);
		this.setOnlyLoadForSelf(false);
		this.setEnableElytraTexture(true);
		this.setAnimatedCapesHandling(AnimatedCapesHandling.ON);
		this.setModProviderHandling(ModProviderHandling.ON);
		this.setKnownModProviderIdsFirstTimeMissing(null);
		
		this.setValidateProfile(null);
		this.setLoadThrottleSuppressSec(null);
		this.setBlockedProviderCapeHashes(null);
		this.setLoadThreads(null);
		this.setPlayerCacheSize(null);
		this.setUseRealPlayerOnlineValidation(null);
	}
	
	public static Config createDefault()
	{
		final Config config = new Config();
		config.reset();
		return config;
	}
	
	// region Get/Set
	
	public String getCurrentPreviewProviderId()
	{
		return this.currentPreviewProviderId;
	}
	
	public void setCurrentPreviewProviderId(final String currentPreviewProviderId)
	{
		this.currentPreviewProviderId = currentPreviewProviderId;
	}
	
	public Set<String> getActiveProviderIds()
	{
		return this.activeProviderIds;
	}
	
	public void setActiveProviderIds(final Collection<String> activeProviderIds)
	{
		this.activeProviderIds = new LinkedHashSet<>(Objects.requireNonNull(activeProviderIds));
	}
	
	public boolean isUseDefaultProvider()
	{
		return this.useDefaultProvider;
	}
	
	public void setUseDefaultProvider(final boolean useDefaultProvider)
	{
		this.useDefaultProvider = useDefaultProvider;
	}
	
	public boolean isOnlyLoadForSelf()
	{
		return this.onlyLoadForSelf;
	}
	
	public void setOnlyLoadForSelf(final boolean onlyLoadForSelf)
	{
		this.onlyLoadForSelf = onlyLoadForSelf;
	}
	
	public boolean isEnableElytraTexture()
	{
		return this.enableElytraTexture;
	}
	
	public void setEnableElytraTexture(final boolean enableElytraTexture)
	{
		this.enableElytraTexture = enableElytraTexture;
	}
	
	public AnimatedCapesHandling getAnimatedCapesHandling()
	{
		return this.animatedCapesHandling;
	}
	
	public void setAnimatedCapesHandling(final AnimatedCapesHandling animatedCapesHandling)
	{
		this.animatedCapesHandling = animatedCapesHandling;
	}
	
	public List<CustomProviderConfig> getCustomProviders()
	{
		return this.customProviders;
	}
	
	public void setCustomProviders(final List<CustomProviderConfig> customProviders)
	{
		this.customProviders = customProviders;
	}
	
	public ModProviderHandling getModProviderHandling()
	{
		return this.modProviderHandling;
	}
	
	public void setModProviderHandling(final ModProviderHandling modProviderHandling)
	{
		this.modProviderHandling = modProviderHandling;
	}
	
	public Map<String, Instant> getKnownModProviderIdsFirstTimeMissing()
	{
		return this.knownModProviderIdsFirstTimeMissing;
	}
	
	public void setKnownModProviderIdsFirstTimeMissing(final Map<String, Instant> knownModProviderIdsFirstTimeMissing)
	{
		this.knownModProviderIdsFirstTimeMissing = knownModProviderIdsFirstTimeMissing;
	}
	
	public Boolean isValidateProfile()
	{
		return this.validateProfile;
	}
	
	public void setValidateProfile(final Boolean validateProfile)
	{
		this.validateProfile = validateProfile;
	}
	
	public Integer getLoadThrottleSuppressSec()
	{
		return this.loadThrottleSuppressSec;
	}
	
	public void setLoadThrottleSuppressSec(final Integer loadThrottleSuppressSec)
	{
		this.loadThrottleSuppressSec = loadThrottleSuppressSec;
	}
	
	public Map<String, Set<Integer>> getBlockedProviderCapeHashes()
	{
		return this.blockedProviderCapeHashes;
	}
	
	public void setBlockedProviderCapeHashes(final Map<String, Set<Integer>> blockedProviderCapeHashes)
	{
		this.blockedProviderCapeHashes = blockedProviderCapeHashes;
	}
	
	public Integer getLoadThreads()
	{
		return this.loadThreads;
	}
	
	public void setLoadThreads(final Integer loadThreads)
	{
		this.loadThreads = loadThreads;
	}
	
	public Integer getPlayerCacheSize()
	{
		return this.playerCacheSize;
	}
	
	public void setPlayerCacheSize(final Integer playerCacheSize)
	{
		this.playerCacheSize = playerCacheSize;
	}
	
	public Boolean getUseRealPlayerOnlineValidation()
	{
		return this.useRealPlayerOnlineValidation;
	}
	
	public void setUseRealPlayerOnlineValidation(final Boolean useRealPlayerOnlineValidation)
	{
		this.useRealPlayerOnlineValidation = useRealPlayerOnlineValidation;
	}
	
	// endregion
}
