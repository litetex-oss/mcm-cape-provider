package net.litetex.capes.menu.preview.render;

import net.minecraft.util.Identifier;


public record PlayerDisplayGuiPayload(
	Identifier bodyTexture,
	Identifier capeTexture,
	Identifier elytraTexture,
	boolean slim
)
{
}
