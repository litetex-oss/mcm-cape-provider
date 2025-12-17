package net.litetex.capes.provider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import net.litetex.capes.Capes;
import net.litetex.capes.texturecache.TextureCache;


public abstract class CacheableCapeProvider implements CapeProvider
{
	protected ResolvedTextureInfo.ByteArrayTextureInfo resolveCacheableTexture(
		final String textureUrl,
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder baseRequestBuilder,
		final String textureResolverId) throws IOException, InterruptedException
	{
		final URI textureUri = URI.create(textureUrl);
		
		final String textureId = this.extractTextureId(textureUri, textureUrl);
		
		final var optCachedTextureInfo = this.textureCache()
			.loadExistingTexture(this.id(), textureId)
			.map(data -> new ResolvedTextureInfo.ByteArrayTextureInfo(data, textureResolverId));
		if(optCachedTextureInfo.isPresent())
		{
			return optCachedTextureInfo.get();
		}
		
		final var textureInfo = this.fetchTexture(
			clientBuilder,
			baseRequestBuilder.copy().uri(textureUri),
			textureResolverId);
		this.textureCache().saveTexture(this.id(), textureId, textureInfo.imageBytes());
		return textureInfo;
	}
	
	protected abstract ResolvedTextureInfo.ByteArrayTextureInfo fetchTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final String textureResolverId) throws IOException, InterruptedException;
	
	protected String extractTextureId(final URI textureUri, final String textureUrl)
	{
		String path = textureUri.getPath();
		path = path.endsWith("/") && path.length() >= 2
			? path.substring(0, path.length() - 2)
			: path;
		if(path.isEmpty())
		{
			return textureUrl;
		}
		
		final int lastSlash = path.lastIndexOf("/");
		path = lastSlash != -1 && lastSlash < path.length() - 2
			? path.substring(path.lastIndexOf("/") + 1)
			: path;
		return path.isEmpty()
			? textureUrl
			: path;
	}
	
	@Override
	public boolean canUseCache()
	{
		return true;
	}
	
	private TextureCache textureCache()
	{
		return Capes.instance().textureCache();
	}
}
