package net.litetex.capes.config;

import java.util.List;
import java.util.Objects;


public record CustomProviderConfig(
	String id,
	String name,
	String uriTemplate,
	boolean animated,
	String changeCapeUrl,
	String homepage,
	List<String> antiFeatures,
	Double rateLimitedReqPerSec
)
{
	public CustomProviderConfig
	{
		Objects.requireNonNull(id);
		Objects.requireNonNull(name);
		Objects.requireNonNull(uriTemplate);
	}
	
	public CustomProviderConfig(final String id, final String name, final String uriTemplate)
	{
		this(id, name, uriTemplate, false, null, null, null, null);
	}
}
