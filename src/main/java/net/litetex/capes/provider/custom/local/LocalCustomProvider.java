package net.litetex.capes.provider.custom.local;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.ResolvedTextureInfo;
import net.litetex.capes.provider.custom.BaseCustomProvider;


public class LocalCustomProvider extends BaseCustomProvider<LocalCustomProviderConfig>
{
	public LocalCustomProvider(final LocalCustomProviderConfig config)
	{
		super(config);
	}
	
	@Override
	protected String getBaseUrlInternal(final GameProfile profile)
	{
		return "";
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile)
	{
		return new ResolvedTextureInfo.ByteArrayTextureInfo(this.config.capeTexture());
	}
	
	@Override
	public double rateLimitedReqPerSec()
	{
		return -1;
	}
}
