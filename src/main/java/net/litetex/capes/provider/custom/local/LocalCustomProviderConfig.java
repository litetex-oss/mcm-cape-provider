package net.litetex.capes.provider.custom.local;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.litetex.capes.provider.custom.BaseCustomProviderConfig;
import net.litetex.capes.provider.custom.Owners;


public record LocalCustomProviderConfig(
	String id,
	String name,
	@Nullable
	String homepage,
	@Nullable
	Owners owners,
	byte[] capeTexture,
	@Nullable
	String textureResolverId
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
	
	public LocalCustomProviderConfig(
		final String id,
		final String name,
		final String homepage,
		final Owners owners,
		final byte[] capeTexture)
	{
		this(id, name, homepage, owners, capeTexture, null);
	}
}
