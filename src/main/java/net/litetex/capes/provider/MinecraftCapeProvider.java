package net.litetex.capes.provider;

import com.mojang.authlib.GameProfile;


public class MinecraftCapeProvider implements CapeProvider
{
	public static final MinecraftCapeProvider INSTANCE = new MinecraftCapeProvider();
	
	@Override
	public String id()
	{
		return "minecraft";
	}
	
	@Override
	public String name()
	{
		return "Minecraft";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return null;
	}
}
