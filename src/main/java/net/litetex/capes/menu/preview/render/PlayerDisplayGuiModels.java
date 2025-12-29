package net.litetex.capes.menu.preview.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.player.PlayerModel;


public record PlayerDisplayGuiModels(
	PlayerModel player,
	ElytraModel elytra,
	ModelPart cape
)
{
}
