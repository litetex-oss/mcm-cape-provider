package net.litetex.capes.handler;

import net.minecraft.util.AssetInfo;


public interface TextureProvider
{
	// Never null!
	AssetInfo.TextureAsset texture();
	
	boolean dynamicIdentifier();
}
