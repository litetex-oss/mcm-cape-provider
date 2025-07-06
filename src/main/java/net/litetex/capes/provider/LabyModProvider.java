package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.minecraft.client.MinecraftClient;


public class LabyModProvider implements CapeProvider
{
	private static final int DEFAULT_TEXTURE_HASH_CODE = -913957301;
	
	@Override
	public String id()
	{
		return "labymod";
	}
	
	@Override
	public String name()
	{
		return "LabyMod";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return "https://dl.labymod.net/capes/" + profile.getId().toString();
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		final ResolvedTextureInfo resolvedTextureInfo =
			CapeProvider.super.resolveTexture(clientBuilder, requestBuilder, profile);
		
		// Filter out default cape
		if(resolvedTextureInfo == null
			|| resolvedTextureInfo.imageBytes() == null
			|| Arrays.hashCode(resolvedTextureInfo.imageBytes()) == DEFAULT_TEXTURE_HASH_CODE
		)
		{
			return null;
		}
		
		return resolvedTextureInfo;
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public String changeCapeUrl(final MinecraftClient client)
	{
		return "https://labymod.net/login";
	}
	
	@Override
	public String homepageUrl()
	{
		return "https://labymod.net";
	}
	
	@Override
	public List<AntiFeature> antiFeatures()
	{
		return List.of(
			AntiFeatures.PAYMENT_TO_UNLOCK_CAPE, // https://labymod.net/en/shop#cosmetics
			AntiFeatures.EXPLICIT, // https://laby.net/cloaks contains content that violates EULA
			AntiFeatures.OVERWRITES
		);
	}
}
