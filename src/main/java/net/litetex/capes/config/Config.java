package net.litetex.capes.config;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.litetex.capes.provider.MinecraftCapesCapeProvider;
import net.litetex.capes.provider.custom.remote.RemoteCustomProviderConfig;


public class Config
{
	private String currentPreviewProviderId;
	// NOTE: Default/Minecraft is always active
	private Set<String> activeProviderIds;
	private boolean useDefaultProvider = true;
	private boolean onlyLoadForSelf;
	private boolean enableElytraTexture;
	private AnimatedCapesHandling animatedCapesHandling = AnimatedCapesHandling.ON;
	private List<RemoteCustomProviderConfig> remoteCustomProviders = List.of();
	
	private boolean loadProvidersFromMods = true;
	private boolean loadSimpleLocalProvidersFromFilesystem = true;
	private boolean activateExternalProvidersOnInitialLoad = true;
	
	private Map<String, Instant> knownAutoActivatingProviderIdsFirstTimeMissing;
	
	// Advanced/Debug options
	private Boolean validateProfile;
	private Integer loadThrottleSuppressSec;
	private Map<String, Set<Integer>> blockedProviderCapeHashes;
	private Integer loadThreads;
	private Integer playerCacheSize;
	private Boolean useRealPlayerOnlineValidation;
	
	private Integer textureCacheDeleteUnusedDays;
	private Integer textureCacheMaxSize;
	
	public void reset()
	{
		this.setCurrentPreviewProviderId(null);
		this.setActiveProviderIds(List.of(MinecraftCapesCapeProvider.ID));
		this.setUseDefaultProvider(true);
		this.setOnlyLoadForSelf(false);
		this.setEnableElytraTexture(true);
		this.setAnimatedCapesHandling(AnimatedCapesHandling.ON);
		
		this.setLoadProvidersFromMods(true);
		this.setLoadSimpleLocalProvidersFromFilesystem(true);
		this.setActivateExternalProvidersOnInitialLoad(true);
		
		this.setKnownAutoActivatingProviderIdsFirstTimeMissing(null);
		
		this.setValidateProfile(null);
		this.setLoadThrottleSuppressSec(null);
		this.setBlockedProviderCapeHashes(null);
		this.setLoadThreads(null);
		this.setPlayerCacheSize(null);
		this.setUseRealPlayerOnlineValidation(null);
		
		this.setTextureCacheDeleteUnusedDays(null);
		this.setTextureCacheMaxSize(null);
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
	
	public List<RemoteCustomProviderConfig> getRemoteCustomProviders()
	{
		return this.remoteCustomProviders;
	}
	
	public void setRemoteCustomProviders(final List<RemoteCustomProviderConfig> remoteCustomProviders)
	{
		this.remoteCustomProviders = remoteCustomProviders;
	}
	
	public boolean isLoadProvidersFromMods()
	{
		return this.loadProvidersFromMods;
	}
	
	public void setLoadProvidersFromMods(final boolean loadProvidersFromMods)
	{
		this.loadProvidersFromMods = loadProvidersFromMods;
	}
	
	public boolean isLoadSimpleLocalProvidersFromFilesystem()
	{
		return this.loadSimpleLocalProvidersFromFilesystem;
	}
	
	public void setLoadSimpleLocalProvidersFromFilesystem(final boolean loadSimpleLocalProvidersFromFilesystem)
	{
		this.loadSimpleLocalProvidersFromFilesystem = loadSimpleLocalProvidersFromFilesystem;
	}
	
	public boolean isActivateExternalProvidersOnInitialLoad()
	{
		return this.activateExternalProvidersOnInitialLoad;
	}
	
	public void setActivateExternalProvidersOnInitialLoad(final boolean activateExternalProvidersOnInitialLoad)
	{
		this.activateExternalProvidersOnInitialLoad = activateExternalProvidersOnInitialLoad;
	}
	
	public Map<String, Instant> getKnownAutoActivatingProviderIdsFirstTimeMissing()
	{
		return this.knownAutoActivatingProviderIdsFirstTimeMissing;
	}
	
	public void setKnownAutoActivatingProviderIdsFirstTimeMissing(
		final Map<String, Instant> knownAutoActivatingProviderIdsFirstTimeMissing)
	{
		this.knownAutoActivatingProviderIdsFirstTimeMissing = knownAutoActivatingProviderIdsFirstTimeMissing;
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
	
	public Integer getTextureCacheDeleteUnusedDays()
	{
		return this.textureCacheDeleteUnusedDays;
	}
	
	public void setTextureCacheDeleteUnusedDays(final Integer textureCacheDeleteUnusedDays)
	{
		this.textureCacheDeleteUnusedDays = textureCacheDeleteUnusedDays;
	}
	
	public Integer getTextureCacheMaxSize()
	{
		return this.textureCacheMaxSize;
	}
	
	public void setTextureCacheMaxSize(final Integer textureCacheMaxSize)
	{
		this.textureCacheMaxSize = textureCacheMaxSize;
	}
	
	// endregion
}
