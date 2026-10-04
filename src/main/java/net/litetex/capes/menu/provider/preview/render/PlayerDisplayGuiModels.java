package net.litetex.capes.menu.provider.preview.render;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;


public record PlayerDisplayGuiModels(
	PlayerModel player,
	ElytraModel elytra,
	ModelPart cape,
	// State used for player and elytra
	AvatarRenderState state
)
{
}
