package net.litetex.capes.access.skin_body_access;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.player.PlayerSkin;


public interface ClientAssetDownloadedTextureAccess
{
	void cpSetImageData(byte[] data);
	
	@Nullable
	byte[] cpGetImageData();
	
	@Nullable
	static byte[] extractBodyImageData(final PlayerSkin playerSkin)
	{
		return playerSkin.body() instanceof final ClientAssetDownloadedTextureAccess clientAssetDownloadedTextureAccess
			? clientAssetDownloadedTextureAccess.cpGetImageData()
			: null;
	}
}
