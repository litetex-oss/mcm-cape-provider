package net.litetex.capes.provider.custom.local;

import java.util.Objects;

import net.litetex.capes.provider.custom.BaseCustomProviderConfig;
import net.litetex.capes.provider.custom.Owners;


public record LocalCustomProviderConfig(
	String id,
	String name,
	Owners owners,
	byte[] capeTexture
) implements BaseCustomProviderConfig
{
	public LocalCustomProviderConfig
	{
		Objects.requireNonNull(id);
		Objects.requireNonNull(name);
		Objects.requireNonNull(capeTexture);
		if(capeTexture.length == 0)
		{
			throw new IllegalArgumentException("empty cape texture");
		}
	}
}
