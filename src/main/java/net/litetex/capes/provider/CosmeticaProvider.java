package net.litetex.capes.provider;

import java.util.List;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.minecraft.client.MinecraftClient;


public class CosmeticaProvider implements CapeProvider
{
	@Override
	public String id()
	{
		return "cosmetica";
	}
	
	@Override
	public String name()
	{
		return "Cosmetica";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return "https://api.cosmetica.cc/get/cloak?username=" + profile.getName()
			+ "&uuid=" + profile.getId().toString()
			+ "&nothirdparty";
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public String changeCapeUrl(final MinecraftClient client)
	{
		return "https://login.cosmetica.cc";
	}
	
	@Override
	public String homepageUrl()
	{
		return "https://cosmetica.cc/";
	}
	
	@Override
	public List<AntiFeature> antiFeatures()
	{
		return List.of(
			AntiFeatures.ABANDONED // Last updated 2024-05
		);
	}
}
