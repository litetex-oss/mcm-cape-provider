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


// This is only a fallback when Fabric's Rendering API somehow is not present
// Usually this class is not loaded to prevent Mixin conflicts
@Mixin(GuiRenderer.class)
public abstract class SpecialGuiElementRendererRegisterGuiRendererMixin
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
		if(PlayerDisplayGuiElementRenderer.REGISTERED.compareAndSet(false, true))
		{
			// Add custom renderer
			final PlayerDisplayGuiElementRenderer renderer = new PlayerDisplayGuiElementRenderer(this.vertexConsumers);
			instance.put(renderer.getElementClass(), renderer);
		}
		
		return instance.buildOrThrow();
	}
	
	@Shadow
	@Final
	private VertexConsumerProvider.Immediate vertexConsumers;
}
