package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;


@Mixin(
	value = CapeLayer.class,
	priority = 974 // Prevent priority conflict with other mods
)
public abstract class CapeFeatureRendererMixin
{
	@Redirect(
		method = "submit*",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entitySolid("
				+ "Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"),
		require = 0 // Might already be set by another mod -> Do not require
	)
	private RenderType fixCapeTransparency(final Identifier texture)
	{
		return RenderTypes.armorCutoutNoCull(texture);
	}
}
