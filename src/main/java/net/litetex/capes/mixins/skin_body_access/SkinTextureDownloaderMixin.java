package net.litetex.capes.mixins.skin_body_access;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.NativeImage;

import net.litetex.capes.access.skin_body_access.ClientAssetDownloadedTextureAccess;
import net.litetex.capes.access.skin_body_access.GameProfileUUIDAccessScope;
import net.litetex.capes.provider.builtin.sneakycapes.pixels.SneakyCapesMarkerPixels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.core.ClientAsset;


@Mixin(SkinTextureDownloader.class)
public abstract class SkinTextureDownloaderMixin
{
	@Unique
	private static final Logger LOG = LoggerFactory.getLogger(SkinTextureDownloaderMixin.class);
	
	@WrapMethod(method = "lambda$downloadAndRegisterSkin$0")
	NativeImage downloadAndRegisterSkin0(
		final Path localCopy,
		final ClientAsset.DownloadedTexture texture,
		final boolean processLegacySkin,
		final Operation<NativeImage> original)
	{
		final NativeImage img = original.call(localCopy, texture, processLegacySkin);
		
		// This can't be directly returned because Minecraft uses (multiple) caches for this request
		// therefore it can't be determined when this is really needed or not
		final ClientAssetDownloadedTextureAccess textureAccess = (ClientAssetDownloadedTextureAccess)(Object)texture;
		
		// processLegacySkin = body
		if(processLegacySkin
			// Body of self is needed for SneakyCapes editor
			&& (Minecraft.getInstance().getUser().getProfileId() == GameProfileUUIDAccessScope.getOrNull()
			// Other bodys are needed for subsequent extraction
			|| SneakyCapesMarkerPixels.validAndMarkersPresent(img)))
		{
			// Dump byte array so that it can be automatically freed up by the garbage collector
			// NativeImage is not freed automatically!
			try(final SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(
				img.getWidth() * img.getHeight()))
			{
				img.writeToChannel(channel);
				textureAccess.cpSetImageData(channel.array());
			}
			catch(final IOException ioe)
			{
				LOG.warn("Failed to grab image data", ioe);
			}
		}
		
		return img;
	}
}
