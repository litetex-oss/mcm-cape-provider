package net.litetex.capes.util;

import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;


public record CapeProviderTextureAsset(Identifier id) implements AssetInfo.TextureAsset
{
	@Override
	public Identifier texturePath()
	{
		return this.id();
	}
}
