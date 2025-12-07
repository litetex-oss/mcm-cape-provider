package net.litetex.capes.menu.preview.render;

import java.util.function.Supplier;

import net.minecraft.core.ClientAsset;


public record PlayerDisplayGuiPayload(
	ClientAsset.Texture bodyTexture,
	Supplier<ClientAsset.Texture> capeTextureSupplier,
	Supplier<ClientAsset.Texture> elytraTextureSupplier,
	boolean slim
)
{
}
