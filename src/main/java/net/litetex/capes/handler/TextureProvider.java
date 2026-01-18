package net.litetex.capes.handler;

import net.minecraft.resources.ResourceLocation;


public interface TextureProvider
{
	// Never null!
	ResourceLocation texture();
	
	boolean dynamicIdentifier();
}
