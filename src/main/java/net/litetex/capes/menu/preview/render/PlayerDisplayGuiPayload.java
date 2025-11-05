package net.litetex.capes.menu.preview.render;

import java.util.function.Supplier;

import net.minecraft.util.AssetInfo;


public record PlayerDisplayGuiPayload(
	AssetInfo.TextureAsset bodyTexture,
	Supplier<AssetInfo.TextureAsset> capeTextureSupplier,
	Supplier<AssetInfo.TextureAsset> elytraTextureSupplier,
	boolean slim
)
{
}
