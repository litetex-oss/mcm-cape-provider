package net.litetex.capes.fabric.compat;

import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.litetex.capes.menu.preview.render.PlayerDisplayGuiElementRenderer;


public class FabricRenderingApiInitializer implements Runnable
{
	@Override
	public void run()
	{
		if(PlayerDisplayGuiElementRenderer.REGISTERED.compareAndSet(false, true))
		{
			SpecialGuiElementRegistry.register(ctx ->
				new PlayerDisplayGuiElementRenderer(ctx.vertexConsumers()));
		}
	}
}
