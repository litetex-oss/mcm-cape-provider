package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;


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
		return "https://api.minecraftcapes.net/profile/" + profile.getId().toString().replace("-", "");
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		requestBuilder
			.setHeader("User-Agent", "minecraftcapes-mod/" + SharedConstants.getGameVersion().name());
		
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
				Boolean.TRUE.equals(responseData.animatedCape())
			);
		}
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public String changeCapeUrl(final MinecraftClient client)
	{
		return this.homepageUrl();
	}
	
	@Override
	public String homepageUrl()
	{
		return "https://minecraftcapes.net";
	}
}
