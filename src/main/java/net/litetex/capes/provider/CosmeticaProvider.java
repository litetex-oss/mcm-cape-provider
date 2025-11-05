package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.minecraft.client.Minecraft;


public class CosmeticaProvider implements CapeProvider
{
	private static final String BASE64_PREFIX = "data:image/png;base64,";
	
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
		return "https://api.cosmetica.cc/v2/get/info"
			+ "?uuid=" + profile.id().toString()
			+ "&nothirdparty"
			+ "&excludemodels";
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		requestBuilder.setHeader("Accept", "application/json");
		
		try(final HttpClient client = clientBuilder.build())
		{
			final HttpResponse<String> response =
				client.send(requestBuilder.GET().build(), HttpResponse.BodyHandlers.ofString());
			
			if(response.statusCode() / 100 != 2)
			{
				return null;
			}
			
			record CapeData(String image)
			{
			}
			record ResponseData(CapeData cape)
			{
			}
			
			final ResponseData responseData = new Gson().fromJson(response.body(), ResponseData.class);
			if(responseData == null
				|| responseData.cape() == null
				|| responseData.cape().image() == null
				|| !responseData.cape().image().startsWith(BASE64_PREFIX))
			{
				return null;
			}
			
			return new ResolvedTextureInfo.Base64TextureInfo(
				responseData.cape().image().substring(BASE64_PREFIX.length())
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
			AntiFeatures.ABANDONED, // Last updated 2024-05
			AntiFeatures.BAD_CONNECTION // Response can take up to 3s
		);
	}
	
	@Override
	public double rateLimitedReqPerSec()
	{
		// has an underperforming backend
		return 10;
	}
}
