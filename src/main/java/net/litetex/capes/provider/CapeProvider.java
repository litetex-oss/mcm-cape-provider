package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.minecraft.client.MinecraftClient;


public interface CapeProvider
{
	String id();
	
	String name();
	
	String getBaseUrl(GameProfile profile);
	
	default ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		try(final HttpClient client = clientBuilder.build())
		{
			final HttpResponse<byte[]> response =
				client.send(requestBuilder.GET().build(), HttpResponse.BodyHandlers.ofByteArray());
			
			if(response.statusCode() / 100 != 2)
			{
				return null;
			}
			
			return new ResolvedTextureInfo.ByteArrayTextureInfo(response.body(), false);
		}
	}
	
	default boolean hasChangeCapeUrl()
	{
		return false;
	}
	
	default String changeCapeUrl(final MinecraftClient client)
	{
		return null;
	}
	
	default String homepageUrl()
	{
		return null;
	}
	
	default List<AntiFeature> antiFeatures()
	{
		return List.of();
	}
}
