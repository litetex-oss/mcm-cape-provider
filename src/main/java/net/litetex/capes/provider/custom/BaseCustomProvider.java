package net.litetex.capes.provider.custom;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.CapeProvider;


public abstract class BaseCustomProvider<C extends BaseCustomProviderConfig> implements CapeProvider
{
	protected final C config;
	
	public BaseCustomProvider(final C config)
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
		final Owners owners = this.config.owners();
		if(owners != null && !owners.owns(profile))
		{
			return null;
		}
		
		return this.getBaseUrlInternal(profile);
	}
	
	protected abstract String getBaseUrlInternal(final GameProfile profile);
}
