package net.litetex.capes.provider.builtin.sneakycapes;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;


public final class TextureManagerReader
{
	public static AbstractTexture getTextureSafe(
		final TextureManager textureManager,
		final Identifier location)
	{
		// Do not use getTexture as it tries to register a new texture if it was not found!
		// This can be the case when the skin was not found (e.g. when using one of the default skins)!
		return textureManager.byPath.get(location);
	}
	
	private TextureManagerReader()
	{
	}
}
