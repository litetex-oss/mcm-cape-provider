package net.litetex.capes.util;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;


public final class GameProfileUtil
{
	public static boolean isSelf(final GameProfile profile)
	{
		return profile.id().equals(Minecraft.getInstance().getUser().getProfileId());
	}
	
	private GameProfileUtil()
	{
	}
}
