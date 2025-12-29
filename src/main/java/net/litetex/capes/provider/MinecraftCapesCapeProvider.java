package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
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
			@SerializedName("animated_cape_url")
			String animatedCapeUrl,
			@SerializedName("cape_url")
			String capeUrl
		)
		{
		}
		
		final ResponseData responseData;
		try(final HttpClient client = clientBuilder.build())
		{
			final HttpResponse<String> response =
				client.send(
					requestBuilder.copy()
						.setHeader("Accept", "application/json")
						.GET()
						.build(),
					HttpResponse.BodyHandlers.ofString());
			
			if(response.statusCode() / 100 != 2)
			{
				return null;
			}
			
			responseData = new Gson().fromJson(response.body(), ResponseData.class);
		}
		if(responseData == null)
		{
			return null;
		}
		
		final String textureUrl = Boolean.TRUE.equals(responseData.animatedCape())
			? responseData.animatedCapeUrl()
			: responseData.capeUrl();
		if(textureUrl == null)
		{
			return null;
		}
		
		return this.resolveCacheableTexture(
			textureUrl,
			clientBuilder,
			requestBuilder,
			responseData.animatedCape() ? AnimatedSpriteTextureResolver.ID : null);
	}
	
	@Override
	protected ResolvedTextureInfo.ByteArrayTextureInfo fetchTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final String textureResolverId) throws IOException, InterruptedException
	{
		return CapeProvider.resolveTextureDefault(clientBuilder, requestBuilder, textureResolverId);
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
