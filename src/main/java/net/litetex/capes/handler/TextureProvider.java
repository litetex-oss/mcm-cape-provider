package net.litetex.capes.handler;

import net.minecraft.core.ClientAsset;


public interface TextureProvider
{
	// Never null!
	ClientAsset.Texture texture();
	
	boolean dynamicIdentifier();
}
