package net.litetex.capes.mixins.skin_body_access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.litetex.capes.access.skin_body_access.ClientAssetDownloadedTextureAccess;
import net.minecraft.core.ClientAsset;


@Mixin(ClientAsset.DownloadedTexture.class)
public abstract class ClientAssetDownloadedTextureMixin implements ClientAssetDownloadedTextureAccess
{
	@Unique
	private byte[] imageData;
	
	@Override
	public void cp$setImageData(final byte[] data)
	{
		this.imageData = data;
	}
	
	@Override
	public byte[] cp$getImageData()
	{
		return this.imageData;
	}
}
