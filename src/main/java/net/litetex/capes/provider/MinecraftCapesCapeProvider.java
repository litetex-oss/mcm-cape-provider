package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;

import net.litetex.capes.handler.textures.AnimatedSpriteTextureResolver;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;


public class MinecraftCapesCapeProvider implements CapeProvider
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
			.setHeader("User-Agent", "minecraftcapes-mod/" + SharedConstants.getCurrentVersion().name())
			.setHeader("Accept", "application/json");
		
		try(final HttpClient client = clientBuilder.build())
		{
			final HttpResponse<String> response =
				client.send(requestBuilder.GET().build(), HttpResponse.BodyHandlers.ofString());
			
			if(response.statusCode() / 100 != 2)
			{
				return null;
			}
			
			record ResponseData(
				Boolean animatedCape,
				Map<String, String> textures
			)
			{
			}
			
			final ResponseData responseData = new Gson().fromJson(response.body(), ResponseData.class);
			
			return new ResolvedTextureInfo.Base64TextureInfo(
				responseData.textures().get("cape"),
				responseData.animatedCape() ? AnimatedSpriteTextureResolver.ID : null
			);
		}
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
