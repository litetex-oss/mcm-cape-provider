package net.litetex.capes.provider;

import com.mojang.authlib.GameProfile;


public class DefaultMinecraftCapeProvider implements CapeProvider
{
	public static final DefaultMinecraftCapeProvider INSTANCE = new DefaultMinecraftCapeProvider();
	
	@Override
	public String id()
	{
		return "default";
	}
	
	@Override
	public String name()
	{
		return "Default / Minecraft";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return null;
	}
}
