package net.litetex.capes.menu.preview.render;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;


@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerDisplayWidget extends PlayerSkinWidget
{
	private final Supplier<PlayerDisplayGuiPayload> payloadSupplier;
	private final Consumer<PlayerDisplayGuiModels> preModelRenderAction;
	
	private final ElytraEntityModel elytraEntityModel;
	private final ModelPart capeModel;
	
	public PlayerDisplayWidget(
		final int width,
		final int height,
		final LoadedEntityModels entityModels,
		final Supplier<PlayerDisplayGuiPayload> payloadSupplier,
		final Consumer<PlayerDisplayGuiModels> preModelRenderAction)
	{
		super(width, height, entityModels, null);
		this.payloadSupplier = payloadSupplier;
		this.preModelRenderAction = preModelRenderAction;
		
		this.elytraEntityModel = new ElytraEntityModel(entityModels.getModelPart(EntityModelLayers.ELYTRA));
		this.capeModel = entityModels.getModelPart(EntityModelLayers.PLAYER_CAPE);
	}
	
	@Override
	protected void renderWidget(final DrawContext context, final int mouseX, final int mouseY, final float deltaTicks)
	{
		final PlayerDisplayGuiPayload payload = this.payloadSupplier.get();
		
		final PlayerDisplayGuiModels models = new PlayerDisplayGuiModels(
			payload.slim() ? this.slimModel : this.wideModel,
			this.elytraEntityModel,
			this.capeModel
		);
		this.preModelRenderAction.accept(models);
		
		this.addToDrawContext(
			context,
			models,
			payload,
			0.97F * this.getHeight() / 2.125F,
			this.xRotation,
			this.yRotation,
			-1.0625F,
			this.getX(),
			this.getY(),
			this.getRight(),
			this.getBottom());
	}
	
	@SuppressWarnings("PMD.ExcessiveParameterList") // Derived from MC code
	public void addToDrawContext(
		final DrawContext context,
		final PlayerDisplayGuiModels models,
		final PlayerDisplayGuiPayload payload,
		final float scale,
		final float xRotation,
		final float yRotation,
		final float yPivot,
		final int x1,
		final int y1,
		final int x2,
		final int y2)
	{
		context.state.addSpecialElement(new PlayerDisplayGuiElementRenderState(
			models,
			payload,
			xRotation,
			yRotation,
			yPivot,
			x1,
			y1,
			x2,
			y2,
			scale,
			context.scissorStack.peekLast()));
	}
}
