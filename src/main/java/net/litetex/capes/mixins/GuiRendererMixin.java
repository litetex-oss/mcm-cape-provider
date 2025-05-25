package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.common.collect.ImmutableMap;

import net.litetex.capes.menu.preview.render.PlayerDisplayGuiElementRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.render.VertexConsumerProvider;


@Mixin(GuiRenderer.class)
public class GuiRendererMixin
{
	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/ImmutableMap$Builder;buildOrThrow()"
				+ "Lcom/google/common/collect/ImmutableMap;",
			remap = false)
	)
	@SuppressWarnings({"rawtypes", "unchecked"})
	private ImmutableMap modifyInit(final ImmutableMap.Builder instance)
	{
		// Add custom renderer
		final PlayerDisplayGuiElementRenderer renderer = new PlayerDisplayGuiElementRenderer(this.vertexConsumers);
		instance.put(renderer.getElementClass(), renderer);
		
		return instance.buildOrThrow();
	}
	
	@Shadow
	@Final
	private VertexConsumerProvider.Immediate vertexConsumers;
}
