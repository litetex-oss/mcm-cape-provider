package net.litetex.capes.util;

import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;


public record CapeProviderTextureAsset(ResourceLocation id) implements ClientAsset.Texture
{
	@Override
	public ResourceLocation texturePath()
	{
		return this.id();
	}
}
