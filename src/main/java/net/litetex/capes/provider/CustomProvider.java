package net.litetex.capes.provider;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.config.CustomProviderConfig;
import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.litetex.capes.provider.antifeature.DefaultAntiFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;


public class CustomProvider implements CapeProvider
{
	private final CustomProviderConfig config;
	
	public CustomProvider(final CustomProviderConfig config)
	{
		this.config = config;
	}
	
	@Override
	public String id()
	{
		return this.config.id();
	}
	
	@Override
	public String name()
	{
		return this.config.name();
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return this.config.uriTemplate()
			.replace("$name", profile.getName())
			.replace("$id", profile.getId().toString())
			.replace("$idNoHyphen", profile.getId().toString().replace("-", ""));
	}
	
	@Override
	public boolean isDefaultAnimatedTexture()
	{
		return this.config.animated();
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return this.config.changeCapeUrl() != null;
	}
	
	@Override
	public String changeCapeUrl(final MinecraftClient client)
	{
		return this.config.changeCapeUrl();
	}
	
	@Override
	public String homepageUrl()
	{
		return this.config.homepage();
	}
	
	@Override
	public List<AntiFeature> antiFeatures()
	{
		if(this.config.antiFeatures() == null)
		{
			return List.of();
		}
		
		return this.config.antiFeatures().stream()
			.filter(Objects::nonNull)
			.map(s -> Optional.ofNullable(AntiFeatures.ALL_DEFAULT.get(s))
				.orElseGet(() -> new DefaultAntiFeature(Text.literal(s))))
			.toList();
	}
	
	@Override
	public double rateLimitedReqPerSec()
	{
		return this.config.rateLimitedReqPerSec() != null
			? this.config.rateLimitedReqPerSec()
			: DEFAULT_RATE_LIMIT_REQ_PER_SEC;
	}
}
