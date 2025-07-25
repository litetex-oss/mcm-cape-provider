package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;


public class WynntilsProvider implements CapeProvider
{
	@Override
	public String id()
	{
		return "wynntils";
	}
	
	@Override
	public String name()
	{
		return "Wynntils";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return "https://athena.wynntils.com/user/getInfo";
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		try(final HttpClient client = clientBuilder.build())
		{
			final JsonObject body = new JsonObject();
			body.addProperty("uuid", profile.getId().toString());
			
			final HttpRequest request = requestBuilder
				// Does UserAgent blocking: https://github.com/Wynntils/athena-backend/pull/36
				.header("User-Agent", "Wynntils Artemis\\3.1.6+MC-1.21.4 (client) FABRIC")
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(body.toString()))
				.build();
			final HttpResponse<String> response =
				client.send(
					request,
					HttpResponse.BodyHandlers.ofString());
			
			if(response.statusCode() / 100 != 2)
			{
				return null;
			}
			
			record WynntilsResponseData(String texture)
			{
			}
			
			final WynntilsResponseData responseData = new Gson().fromJson(
				JsonParser.parseString(response.body())
					.getAsJsonObject()
					.getAsJsonObject("user")
					.getAsJsonObject("cosmetics"),
				WynntilsResponseData.class
			);
			if(responseData == null)
			{
				return null;
			}
			
			return new ResolvedTextureInfo.Base64TextureInfo(responseData.texture(), false);
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
		return "https://account.wynntils.com";
	}
	
	@Override
	public String homepageUrl()
	{
		return "https://wynntils.com";
	}
	
	@Override
	public double rateLimitedReqPerSec()
	{
		// Wynntils has a very underperforming backend
		return 4;
	}
}
