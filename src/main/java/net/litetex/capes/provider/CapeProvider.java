package net.litetex.capes.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.minecraft.client.MinecraftClient;


public interface CapeProvider
{
	double DEFAULT_RATE_LIMIT_REQ_PER_SEC = 20;
	
	int DEFAULT_MAX_DOWNLOAD_BYTES = 10_000_000; // 10 MB
	
	String id();
	
	String name();
	
	String getBaseUrl(GameProfile profile);
	
	default ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		return resolveTextureDefault(clientBuilder, requestBuilder, this.textureResolverId());
	}
	
	default String textureResolverId()
	{
		return null;
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
	
	default double rateLimitedReqPerSec()
	{
		return DEFAULT_RATE_LIMIT_REQ_PER_SEC;
	}
	
	static ResolvedTextureInfo.ByteArrayTextureInfo resolveTextureDefault(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final String textureResolverId) throws IOException, InterruptedException
	{
		try(final HttpClient client = clientBuilder.build())
		{
			final HttpResponse<InputStream> response =
				client.send(requestBuilder.GET().build(), HttpResponse.BodyHandlers.ofInputStream());
			
			if(response.statusCode() / 100 != 2)
			{
				return null;
			}
			
			try(final BoundedInputStream cappedIS = BoundedInputStream.builder()
				.setInputStream(response.body())
				.setMaxCount(DEFAULT_MAX_DOWNLOAD_BYTES)
				.get())
			{
				return new ResolvedTextureInfo.ByteArrayTextureInfo(
					IOUtils.toByteArray(cappedIS),
					textureResolverId);
			}
		}
	}
}
