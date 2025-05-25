package net.litetex.capes.menu.preview.render;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;


public record PlayerDisplayGuiModels(
	PlayerEntityModel player,
	ElytraEntityModel elytra,
	ModelPart cape
)
{
}
