package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
		return "https://api.cosmetica.cc/v2/get/info?uuid=" + profile.getId().toString();
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		try(final HttpClient client = clientBuilder.build())
		{
			requestBuilder.header("Accept", "application/json");
			final HttpRequest request = requestBuilder.GET().build();
			final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if(response.statusCode() / 100 != 2)
			{
				return null;
			}
			final JsonObject root;
			try
			{
				root = JsonParser.parseString(response.body()).getAsJsonObject();
			}
			catch(final Exception ex)
			{
				return null;
			}
			if(root == null)
			{
				return null;
			}
			String texture = getImage(root, "capes");
			if(texture == null)
			{
				texture = getImage(root, "cape");
			}
			if(texture == null || texture.isEmpty())
			{
				return null;
			}
			texture = stripDataUri(texture);
			return new ResolvedTextureInfo.Base64TextureInfo(texture);
		}
	}
	
	private static String getImage(final JsonObject root, final String parentKey)
	{
		if(root.has(parentKey) && !root.get(parentKey).isJsonNull())
		{
			final JsonObject obj = root.getAsJsonObject(parentKey);
			if(obj != null && obj.has("image") && !obj.get("image").isJsonNull())
			{
				final String s = obj.get("image").getAsString();
				if(s != null && !s.isEmpty())
				{
					return s;
				}
			}
		}
		return null;
	}
	
	private static String stripDataUri(final String value)
	{
		if(value != null && value.startsWith("data:"))
		{
			final int commaIdx = value.indexOf(',');
			if(commaIdx > 0 && commaIdx + 1 < value.length())
			{
				return value.substring(commaIdx + 1);
			}
		}
		return value;
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
