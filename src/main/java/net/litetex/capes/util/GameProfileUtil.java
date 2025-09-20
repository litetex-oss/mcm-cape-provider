package net.litetex.capes.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;


public final class GameProfileUtil
{
	public static boolean isSelf(final GameProfile profile)
	{
		return profile.id().equals(MinecraftClient.getInstance().getSession().getUuidOrNull());
	}
	
	private GameProfileUtil()
	{
	}
}
