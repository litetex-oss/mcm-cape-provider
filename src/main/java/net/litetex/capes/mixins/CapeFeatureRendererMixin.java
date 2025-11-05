package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;


@Mixin(CapeLayer.class)
public abstract class CapeFeatureRendererMixin
{
	@Redirect(method = "submit*", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/renderer/rendertype/RenderTypes;entitySolid("
			+ "Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
	private RenderType fixCapeTransparency(final Identifier texture)
	{
		return RenderTypes.armorCutoutNoCull(texture);
	}
}
