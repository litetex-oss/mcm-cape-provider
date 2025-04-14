package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.util.Identifier;


@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin
{
	@Redirect(method = "render*", at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/render/RenderLayer;getEntitySolid(Lnet/minecraft/util/Identifier;)"
			+ "Lnet/minecraft/client/render/RenderLayer;"))
	private RenderLayer fixCapeTransparency(final Identifier texture)
	{
		return RenderLayer.getArmorCutoutNoCull(texture);
	}
}
