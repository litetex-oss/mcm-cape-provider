package net.litetex.capes.provider;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.entity.player.PlayerSkin;


// NOTE: This code is derived from
// https://codeberg.org/Penguin_Spy/sneaky_capes/src/commit/43f597ecdf32e0e2cd46bdecefb85ebf0a847df8/src/main/java/dev/penguinspy/sneaky_capes/mixin/PlayerSkinManagerMixin.java
public class SneakyCapesCapeProvider implements CapeProvider
{
	static final List<MarkerPixelInfo> MARKER_PIXELS = List.of(
		new MarkerPixelInfo(60, 48, 0xFFFFF42F),
		new MarkerPixelInfo(60, 49, 0xFFFFFFFF),
		new MarkerPixelInfo(60, 50, 0xFF9C59D1),
		new MarkerPixelInfo(60, 51, 0xFF292929)
	);
	static final int CAPE_WIDTH = 64;
	static final int CAPE_HEIGHT = 32;
	
	@Override
	public String id()
	{
		return "sneaky-capes";
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
		
		if(!(client.getTextureManager().getTexture(playerSkin.body().id())
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
			|| !this.markersPresent(bodyImg))
		{
			return null;
		}
		
		try(final SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(CAPE_WIDTH * CAPE_HEIGHT))
		{
			this.extractCapeTextureFromUnusedBitsInBodyTexture(bodyImg).writeToChannel(channel);
			return new ResolvedTextureInfo.ByteArrayTextureInfo(channel.array());
		}
	}
	
	private boolean markersPresent(final NativeImage img)
	{
		return MARKER_PIXELS.stream()
			.allMatch(marker -> img.getPixel(marker.x(), marker.y()) == marker.rgba());
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	private NativeImage extractCapeTextureFromUnusedBitsInBodyTexture(final NativeImage body)
	{
		final NativeImage cape = new NativeImage(CAPE_WIDTH, CAPE_HEIGHT, true);
		
		// copy cape from skin
		// front
		body.copyRect(cape, 56, 16, 1, 1, 8, 16, false, false);
		body.copyRect(cape, 62, 0, 9, 1, 2, 8, false, false);
		body.copyRect(cape, 60, 0, 9, 9, 2, 8, false, false);
		// back
		body.copyRect(cape, 56, 32, 12, 1, 8, 16, false, false);
		body.copyRect(cape, 58, 0, 20, 1, 2, 8, false, false);
		body.copyRect(cape, 56, 0, 20, 9, 2, 8, false, false);
		// left edge
		body.copyRect(cape, 39, 0, 0, 1, 1, 8, false, false);
		body.copyRect(cape, 38, 0, 0, 9, 1, 8, false, false);
		// right edge
		body.copyRect(cape, 37, 0, 11, 1, 1, 8, false, false);
		body.copyRect(cape, 36, 0, 11, 9, 1, 8, false, false);
		// top edge
		body.copyRect(cape, 0, 48, 1, 0, 4, 1, false, false);
		body.copyRect(cape, 12, 48, 5, 0, 6, 1, false, false);
		// bottom edge
		body.copyRect(cape, 0, 49, 11, 0, 4, 1, false, false);
		body.copyRect(cape, 12, 49, 15, 0, 6, 1, false, false);
		
		// copy elytra from skin
		// front  (10x20)
		body.copyRect(cape, 0, 0, 36, 2, 8, 8, false, false);
		body.copyRect(cape, 24, 0, 44, 2, 2, 8, false, false);
		body.copyRect(cape, 26, 0, 36, 10, 10, 8, false, false);
		body.copyRect(cape, 44, 48, 36, 18, 8, 4, false, false);
		body.copyRect(cape, 62, 48, 44, 18, 2, 4, false, false);
		// inside edge (2x12)
		body.copyRect(cape, 18, 48, 34, 2, 2, 4, false, false);
		body.copyRect(cape, 28, 48, 34, 6, 2, 4, false, false);
		body.copyRect(cape, 30, 48, 34, 10, 2, 4, false, false);
		// outside edge (1x12)
		body.copyRect(cape, 32, 48, 22, 10, 1, 4, false, false);
		body.copyRect(cape, 33, 48, 22, 14, 1, 4, false, false);
		body.copyRect(cape, 34, 48, 22, 18, 1, 4, false, false);
		// top edge (4x2)
		body.copyRect(cape, 0, 50, 30, 0, 4, 2, false, false);
		// bottom edge (6x2)
		body.copyRect(cape, 12, 50, 34, 0, 6, 2, false, false);
		
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
		return -1;
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public String changeCapeUrl(final Minecraft client)
	{
		return "https://penguinspy.neocities.org/projects/loom/";
	}
	
	@Override
	public String homepageUrl()
	{
		return "https://modrinth.com/mod/sneaky_capes";
	}
	
	record MarkerPixelInfo(
		int x,
		int y,
		int rgba
	)
	{
	}
}
