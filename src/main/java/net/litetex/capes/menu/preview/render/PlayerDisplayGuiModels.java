package net.litetex.capes.menu.preview.render;

import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;


public record PlayerDisplayGuiModels(
	PlayerModel player,
	ElytraModel elytra,
	ModelPart cape
)
{
}
