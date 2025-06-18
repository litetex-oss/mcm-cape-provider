package net.litetex.capes.config;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.litetex.capes.provider.MinecraftCapesCapeProvider;
import net.litetex.capes.provider.OptiFineCapeProvider;


public class Config
{
	private String currentPreviewProviderId;
	// NOTE: Default/Minecraft is always active
	private Set<String> activeProviderIds;
	private boolean enableElytraTexture;
	private AnimatedTexturesHandling animatedTexturesHandling = AnimatedTexturesHandling.ON;
	private List<CustomProviderConfig> customProviders = List.of();
	
	// Advanced/Debug options
	private Boolean validateProfile;
	private Integer loadThrottleSuppressSec;
	
	public void reset()
	{
		this.setCurrentPreviewProviderId(null);
		this.setActiveProviderIds(List.of(MinecraftCapesCapeProvider.ID, OptiFineCapeProvider.ID));
		this.setEnableElytraTexture(true);
		this.setAnimatedTexturesHandling(AnimatedTexturesHandling.ON);
		
		this.setValidateProfile(null);
		this.setLoadThrottleSuppressSec(null);
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
	
	public boolean isEnableElytraTexture()
	{
		return this.enableElytraTexture;
	}
	
	public void setEnableElytraTexture(final boolean enableElytraTexture)
	{
		this.enableElytraTexture = enableElytraTexture;
	}
	
	public AnimatedTexturesHandling getAnimatedTexturesHandling()
	{
		return this.animatedTexturesHandling;
	}
	
	public void setAnimatedTexturesHandling(final AnimatedTexturesHandling animatedTexturesHandling)
	{
		this.animatedTexturesHandling = animatedTexturesHandling;
	}
	
	public List<CustomProviderConfig> getCustomProviders()
	{
		return this.customProviders;
	}
	
	public void setCustomProviders(final List<CustomProviderConfig> customProviders)
	{
		this.customProviders = customProviders;
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
	
	// endregion
}
