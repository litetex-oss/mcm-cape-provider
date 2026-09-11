package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Optional;

import com.google.gson.annotations.SerializedName;
import com.mojang.authlib.GameProfile;

import net.litetex.capes.handler.textures.AnimatedSpriteTextureResolver;
import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.minecraft.client.Minecraft;


public class CosmeticaProvider extends CacheableCapeProvider
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
		return "https://api.cloaks.gg/users/" + profile.id().toString() + "/cape";
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		record CloakResponseData(
			int frames,
			@SerializedName("texture")
			String textureUrl
		)
		{
		}
		
		record ResponseData(
			CloakResponseData cloak
		)
		{
		}
		
		final Optional<CloakResponseData> optCloakResponseData = Optional.ofNullable(
				this.downloadJSON(clientBuilder, requestBuilder, ResponseData.class))
			.map(ResponseData::cloak);
		
		if(optCloakResponseData.isEmpty())
		{
			return null;
		}
		
		final CloakResponseData cloakResponseData = optCloakResponseData.orElseThrow();
		if(cloakResponseData.textureUrl() == null)
		{
			return null;
		}
		
		return this.resolveCacheableTexture(
			cloakResponseData.textureUrl(),
			clientBuilder,
			requestBuilder,
			cloakResponseData.frames() > 1 ? AnimatedSpriteTextureResolver.ID : null);
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public String changeCapeUrl(final Minecraft client)
	{
		return "https://cosmetica.cc/login";
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
