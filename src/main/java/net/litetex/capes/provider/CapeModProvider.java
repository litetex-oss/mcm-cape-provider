package net.litetex.capes.provider;

import java.util.List;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.minecraft.client.MinecraftClient;


public class CapeModProvider implements CapeProvider
{
	@Override
	public String id()
	{
		return "capemod";
	}
	
	@Override
	public String name()
	{
		return "CapeMod";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return "https://capes.capemod.com/api/cape/?uuid=" + profile.profile.getId().toString()
			+ "&service=capeprovider";
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public String changeCapeUrl(final MinecraftClient client)
	{
		return "https://capemod.com/dashboard";
	}
	
	@Override
	public String homepageUrl()
	{
		return "https://capemod.com/";
	}
}
