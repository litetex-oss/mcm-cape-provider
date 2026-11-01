package net.litetex.capes.provider.builtin.sneakycapes;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;

import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.ResolvedTextureInfo;
import net.litetex.capes.provider.builtin.sneakycapes.menu.SneakyCapesEditScreen;
import net.litetex.capes.provider.builtin.sneakycapes.pixels.SneakyCapesCopyPixels;
import net.litetex.capes.provider.builtin.sneakycapes.pixels.SneakyCapesMarkerPixels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.entity.player.PlayerSkin;


// NOTE: This code is derived from
// https://codeberg.org/Penguin_Spy/sneaky_capes/src/commit/43f597ecdf32e0e2cd46bdecefb85ebf0a847df8/src/main/java/dev/penguinspy/sneaky_capes/mixin/PlayerSkinManagerMixin.java
public class SneakyCapesCapeProvider implements CapeProvider
{
	public static final String ID = "sneaky-capes";
	
	static final int CAPE_WIDTH = 64;
	static final int CAPE_HEIGHT = 32;
	
	@Override
	public String id()
	{
		return ID;
	}
	
	@Override
	public String name()
	{
		return "Sneaky Capes";
	}
	
	@Override
	public ResolvedTextureInfo resolveTexture(
		final HttpClient.Builder clientBuilder,
		final HttpRequest.Builder requestBuilder,
		final GameProfile profile) throws IOException, InterruptedException
	{
		final Minecraft client = Minecraft.getInstance();
		
		final Optional<PlayerSkin> optPlayerSkin;
		try
		{
			optPlayerSkin = client.getSkinManager().get(profile).get(10, TimeUnit.SECONDS);
		}
		catch(final ExecutionException | TimeoutException e)
		{
			throw new IllegalStateException("Failed to get skin", e);
		}
		
		if(optPlayerSkin.isEmpty())
		{
			return null;
		}
		
		final PlayerSkin playerSkin = optPlayerSkin.orElseThrow();
		
		if(!(TextureManagerReader.getTextureSafe(client.getTextureManager(), playerSkin.body().id())
			instanceof final DynamicTexture dynamicTexture))
		{
			return null;
		}
		
		final NativeImage bodyImg = dynamicTexture.getPixels();
		if(bodyImg == null)
		{
			return null;
		}
		
		// Check if skin is legacy format
		if(bodyImg.getWidth() != 64
			|| bodyImg.getHeight() != 64
			// Markers must be present
			|| !SneakyCapesMarkerPixels.markersPresent(bodyImg))
		{
			return null;
		}
		
		try(final SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(CAPE_WIDTH * CAPE_HEIGHT))
		{
			this.extractCapeTextureFromUnusedBitsInBodyTexture(bodyImg).writeToChannel(channel);
			return new ResolvedTextureInfo.ByteArrayTextureInfo(channel.array());
		}
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	private NativeImage extractCapeTextureFromUnusedBitsInBodyTexture(final NativeImage body)
	{
		final NativeImage cape = new NativeImage(CAPE_WIDTH, CAPE_HEIGHT, true);
		
		SneakyCapesCopyPixels.bodyToCape(body, cape);
		
		return cape;
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return "";
	}
	
	@Override
	public double rateLimitedReqPerSec()
	{
		return NO_RATE_LIMIT_REQ_PER_SEC;
	}
	
	@Override
	public boolean hasChangeCapeScreen()
	{
		return true;
	}
	
	@Override
	public Screen changeCapeScreen(final Minecraft client, final Screen lastScreen)
	{
		return new SneakyCapesEditScreen(client, lastScreen);
	}
	
	@Override
	public URI homepageUri()
	{
		return URI.create("https://modrinth.com/mod/sneaky_capes");
	}
}
