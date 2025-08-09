package net.litetex.capes.menu.preview.render;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joml.Matrix4fStack;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;


@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerDisplayGuiElementRenderer extends SpecialGuiElementRenderer<PlayerDisplayGuiElementRenderState>
{
	public static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
	private static final int LIGHT = 0xF000F0;
	
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
		
		this.renderParts(state.payload(), state.models(), matrixStack);
		
		this.vertexConsumers.draw();
		matrix4fStack.popMatrix();
	}
	
	protected void renderParts(
		final PlayerDisplayGuiPayload payload,
		final PlayerDisplayGuiModels models,
		final MatrixStack matrixStack)
	{
		if(payload.bodyTexture() != null)
		{
			this.render(
				models.player(),
				matrixStack,
				this.vertexConsumers.getBuffer(models.player().getLayer(payload.bodyTexture())));
		}
		
		if(payload.elytraTextureSupplier() != null)
		{
			this.extractFromSupplierAndRender(
				payload.elytraTextureSupplier(), matrixStack, id ->
				{
					matrixStack.translate(0.0f, 0.0f, 0.125f);
					
					this.render(
						models.elytra(),
						matrixStack,
						ItemRenderer.getArmorGlintConsumer(
							this.vertexConsumers,
							RenderLayer.getArmorCutoutNoCull(id),
							false));
				});
		}
		else if(payload.capeTextureSupplier() != null)
		{
			this.extractFromSupplierAndRender(
				payload.capeTextureSupplier(), matrixStack, id ->
				{
					matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0f));
					
					this.render(
						models.cape().getChild("body").getChild("cape"),
						matrixStack,
						this.vertexConsumers.getBuffer(RenderLayer.getArmorCutoutNoCull(id))
					);
				});
		}
	}
	
	protected void extractFromSupplierAndRender(
		final Supplier<Identifier> supplier,
		final MatrixStack matrixStack,
		final Consumer<Identifier> renderer)
	{
		final Identifier id = supplier.get();
		if(id != null)
		{
			matrixStack.push();
			
			renderer.accept(id);
			
			matrixStack.pop();
		}
	}
	
	protected void render(final Model model, final MatrixStack stack, final VertexConsumer c)
	{
		model.render(stack, c, LIGHT, OverlayTexture.DEFAULT_UV);
	}
	
	protected void render(final ModelPart modelPart, final MatrixStack stack, final VertexConsumer c)
	{
		modelPart.render(stack, c, LIGHT, OverlayTexture.DEFAULT_UV);
	}
	
	@Override
	protected String getName()
	{
		return "player display";
	}
}
