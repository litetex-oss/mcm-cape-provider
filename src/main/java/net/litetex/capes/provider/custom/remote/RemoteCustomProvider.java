package net.litetex.capes.provider.custom.remote;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.litetex.capes.provider.antifeature.DefaultAntiFeature;
import net.litetex.capes.provider.custom.BaseCustomProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;


public class RemoteCustomProvider extends BaseCustomProvider<RemoteCustomProviderConfig>
{
	public RemoteCustomProvider(final RemoteCustomProviderConfig config)
	{
		super(config);
	}
	
	@Override
	protected String getBaseUrlInternal(final GameProfile profile)
	{
		final String idString = profile.id().toString();
		String uriTemplate = this.config.uriTemplate();
		if(uriTemplate.indexOf('$') != -1)
		{
			uriTemplate = this.fillInTemplate(uriTemplate, '$', idString, profile);
		}
		else if(uriTemplate.indexOf('§') != -1)
		{
			uriTemplate = this.fillInTemplate(uriTemplate, '§', idString, profile);
		}
		
		return uriTemplate;
	}
	
	protected String fillInTemplate(
		final String uriTemplate,
		final char prefix,
		final String idString,
		final GameProfile profile)
	{
		return uriTemplate
			.replace(prefix + "name", profile.name())
			.replace(prefix + "id", idString)
			.replace(prefix + "idNoHyphen", idString.replace("-", ""));
	}
	
	@Override
	public String textureResolverId()
	{
		final String textureResolverId = this.config.textureResolverId();
		if(textureResolverId != null && !textureResolverId.isEmpty())
		{
			return textureResolverId;
		}
		return null;
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return this.config.changeCapeUrl() != null;
	}
	
	@Override
	public String changeCapeUrl(final Minecraft client)
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
				.orElseGet(() -> new DefaultAntiFeature(Component.literal(s))))
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
