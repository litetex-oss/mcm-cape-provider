package net.litetex.capes.util;

import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;


public record CapeProviderTextureAsset(Identifier id) implements ClientAsset.Texture
{
	@Override
	public Identifier texturePath()
	{
		return this.id();
	}
}
