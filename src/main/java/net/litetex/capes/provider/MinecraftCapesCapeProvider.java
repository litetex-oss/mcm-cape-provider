package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;

import net.litetex.capes.handler.textures.AnimatedSpriteTextureResolver;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;


public class MinecraftCapesCapeProvider extends CacheableCapeProvider
{
	public static final String ID = "minecraftcapes";
	
	@Override
	public String id()
	{
		return ID;
	}
	
	@Override
	public String name()
	{
		return "MinecraftCapes";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return "https://api.minecraftcapes.net/profile/" + profile.id().toString().replace("-", "");
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		requestBuilder
			.setHeader("User-Agent", "minecraftcapes-mod/" + SharedConstants.getCurrentVersion().name());
		
		record ResponseData(
			Boolean animatedCape,
			@SerializedName("cape_url")
			String capeUrl
		)
		{
		}
		
		final ResponseData responseData = this.downloadJSON(clientBuilder, requestBuilder, ResponseData.class);
		if(responseData == null || responseData.capeUrl() == null)
		{
			return null;
		}
		
		return this.resolveCacheableTexture(
			responseData.capeUrl(),
			clientBuilder,
			requestBuilder,
			responseData.animatedCape() ? AnimatedSpriteTextureResolver.ID : null);
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public String changeCapeUrl(final Minecraft client)
	{
		return this.homepageUrl();
	}
	
	@Override
	public String homepageUrl()
	{
		return "https://minecraftcapes.net";
	}
}
