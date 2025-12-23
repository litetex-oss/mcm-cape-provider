package net.litetex.capes.provider.custom.remote;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.litetex.capes.provider.custom.BaseCustomProviderConfig;
import net.litetex.capes.provider.custom.Owners;


public record RemoteCustomProviderConfig(
	String id,
	String name,
	Owners owners,
	String uriTemplate,
	@Nullable
	String textureResolverId,
	String changeCapeUrl,
	String homepage,
	List<String> antiFeatures,
	Double rateLimitedReqPerSec
) implements BaseCustomProviderConfig
{
	public RemoteCustomProviderConfig
	{
		Objects.requireNonNull(id);
		Objects.requireNonNull(name);
		Objects.requireNonNull(uriTemplate);
		if(uriTemplate.isEmpty())
		{
			throw new IllegalArgumentException("uri is empty");
		}
	}
	
	public RemoteCustomProviderConfig(final String id, final String name, final String uriTemplate)
	{
		this(id, name, null, uriTemplate, null, null, null, null, null);
	}
}
