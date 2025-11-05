package net.litetex.capes.menu.preview.render;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joml.Matrix4fStack;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.ResourceLocation;


@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerDisplayGuiElementRenderer extends PictureInPictureRenderer<PlayerDisplayGuiElementRenderState>
{
	private static final int LIGHT = 0xF000F0;
	
	public PlayerDisplayGuiElementRenderer(final MultiBufferSource.BufferSource immediate)
	{
		super(immediate);
	}
	
	@Override
	public Class<PlayerDisplayGuiElementRenderState> getRenderStateClass()
	{
		return PlayerDisplayGuiElementRenderState.class;
	}
	
	@Override
	protected void render(
		final PlayerDisplayGuiElementRenderState state,
		final PoseStack matrixStack)
	{
		Minecraft.getInstance().gameRenderer.getLighting()
			.setupFor(Lighting.Entry.PLAYER_SKIN);
		
		final int windowScaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
		
		final Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
		matrix4fStack.pushMatrix();
		final float f = state.scale() * windowScaleFactor;
		matrix4fStack.rotateAround(
			Axis.XP.rotationDegrees(state.xRotation()),
			0.0F,
			f * -state.yPivot(),
			0.0F
		);
		matrixStack.mulPose(Axis.YP.rotationDegrees(-state.yRotation()));
		matrixStack.translate(0.0F, -1.6010001F, 0.0F);
		
		this.renderParts(state.payload(), state.models(), matrixStack);
		
		this.bufferSource.endBatch();
		matrix4fStack.popMatrix();
	}
	
	protected void renderParts(
		final PlayerDisplayGuiPayload payload,
		final PlayerDisplayGuiModels models,
		final PoseStack matrixStack)
	{
		if(payload.bodyTexture() != null)
		{
			this.render(
				models.player(),
				matrixStack,
				this.bufferSource.getBuffer(models.player().renderType(payload.bodyTexture().texturePath())));
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
						this.bufferSource.getBuffer(RenderType.armorCutoutNoCull(id)));
				});
		}
		else if(payload.capeTextureSupplier() != null)
		{
			this.extractFromSupplierAndRender(
				payload.capeTextureSupplier(), matrixStack, id ->
				{
					matrixStack.mulPose(Axis.XP.rotationDegrees(6.0f));
					
					this.render(
						models.cape().getChild("body").getChild("cape"),
						matrixStack,
						this.bufferSource.getBuffer(RenderType.armorCutoutNoCull(id))
					);
				});
		}
	}
	
	protected void extractFromSupplierAndRender(
		final Supplier<ClientAsset.Texture> supplier,
		final PoseStack matrixStack,
		final Consumer<ResourceLocation> renderer)
	{
		final ClientAsset.Texture textureAsset = supplier.get();
		if(textureAsset == null)
		{
			return;
		}
		
		final ResourceLocation id = textureAsset.texturePath();
		if(id == null)
		{
			return;
		}
		
		matrixStack.pushPose();
		renderer.accept(id);
		matrixStack.popPose();
	}
	
	protected void render(final Model<?> model, final PoseStack stack, final VertexConsumer c)
	{
		model.renderToBuffer(stack, c, LIGHT, OverlayTexture.NO_OVERLAY);
	}
	
	protected void render(final ModelPart modelPart, final PoseStack stack, final VertexConsumer c)
	{
		modelPart.render(stack, c, LIGHT, OverlayTexture.NO_OVERLAY);
	}
	
	@Override
	protected String getTextureLabel()
	{
		return "player display";
	}
}
