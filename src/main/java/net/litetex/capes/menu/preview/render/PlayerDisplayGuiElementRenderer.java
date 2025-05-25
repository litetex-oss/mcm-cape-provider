package net.litetex.capes.menu.preview.render;

import org.joml.Matrix4fStack;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;


@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerDisplayGuiElementRenderer extends SpecialGuiElementRenderer<PlayerDisplayGuiElementRenderState>
{
	public PlayerDisplayGuiElementRenderer(final VertexConsumerProvider.Immediate immediate)
	{
		super(immediate);
	}
	
	@Override
	public Class<PlayerDisplayGuiElementRenderState> getElementClass()
	{
		return PlayerDisplayGuiElementRenderState.class;
	}
	
	@Override
	protected void render(
		final PlayerDisplayGuiElementRenderState state,
		final MatrixStack matrixStack)
	{
		MinecraftClient.getInstance().gameRenderer.getDiffuseLighting()
			.setShaderLights(DiffuseLighting.Type.PLAYER_SKIN);
		
		final int windowScaleFactor = MinecraftClient.getInstance().getWindow().getScaleFactor();
		
		final Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		final float f = state.scale() * windowScaleFactor;
		matrix4fStack.rotateAround(
			RotationAxis.POSITIVE_X.rotationDegrees(state.xRotation()),
			0.0F,
			f * -state.yPivot(),
			0.0F
		);
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-state.yRotation()));
		matrixStack.translate(0.0F, -1.6010001F, 0.0F);
		final RenderLayer renderLayer =
			state.playerModel().getLayer(state.texture());
		state.playerModel()
			.render(matrixStack, this.vertexConsumers.getBuffer(renderLayer), 15728880, OverlayTexture.DEFAULT_UV);
		this.vertexConsumers.draw();
		matrix4fStack.popMatrix();
	}
	
	@Override
	protected String getName()
	{
		return "player display";
	}
}
