package net.litetex.capes.menu.preview.render;

import java.util.function.Supplier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PlayerSkinWidget;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;


@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerDisplayWidget extends PlayerSkinWidget
{
	public PlayerDisplayWidget(
		final int width,
		final int height,
		final LoadedEntityModels entityModels,
		final Supplier<SkinTextures> skinSupplier)
	{
		super(width, height, entityModels, skinSupplier);
	}
	
	@Override
	protected void renderWidget(final DrawContext context, final int mouseX, final int mouseY, final float deltaTicks)
	{
		final SkinTextures skinTextures = this.skinSupplier.get();
		this.addToDrawContext(
			context,
			skinTextures.model() == SkinTextures.Model.SLIM ? this.slimModel : this.wideModel,
			skinTextures.texture(),
			0.97F * this.getHeight() / 2.125F,
			this.xRotation,
			this.yRotation,
			-1.0625F,
			this.getX(),
			this.getY(),
			this.getRight(),
			this.getBottom());
	}
	
	public void addToDrawContext(
		final DrawContext context,
		final PlayerEntityModel playerModel,
		final Identifier texture,
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
			playerModel,
			texture,
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
