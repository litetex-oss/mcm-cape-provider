package net.litetex.capes.menu.preview.render;

import java.util.function.Supplier;

import net.minecraft.util.Identifier;


public record PlayerDisplayGuiPayload(
	Identifier bodyTexture,
	Supplier<Identifier> capeTextureSupplier,
	Supplier<Identifier> elytraTextureSupplier,
	boolean slim
)
{
}
