package net.litetex.capes.menu.provider.preview.render;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joml.Matrix4fStack;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;


// See also: GuiSkinRenderer
@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerDisplayGuiElementRenderer extends PictureInPictureRenderer<PlayerDisplayGuiElementRenderState>
{
	private static final int LIGHT = 0xF000F0;
	
	public PlayerDisplayGuiElementRenderer()
	{
		super();
	}
	
	@Override
	public Class<PlayerDisplayGuiElementRenderState> getRenderStateClass()
	{
		return PlayerDisplayGuiElementRenderState.class;
	}
	
	@Override
	protected void renderToTexture(
		final PlayerDisplayGuiElementRenderState skinState,
		final PoseStack modelStack,
		final SubmitNodeCollector submitNodeCollector)
	{
		Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.PLAYER_SKIN);
		
		final int windowScaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
		
		final Matrix4fStack viewStack = RenderSystem.getModelViewStack();
		viewStack.pushMatrix();
		final float scale = skinState.scale() * windowScaleFactor;
		// MC-308168 Rotation is not working
		viewStack.rotateAround(
			Axis.XP.rotationDegrees(skinState.xRotation()),
			0.0F,
			scale * -skinState.yPivot(),
			0.0F
		);
		modelStack.mulPose(Axis.YP.rotationDegrees(-skinState.yRotation()));
		modelStack.translate(0.0F, -1.6010001F, 0.0F);
		
		this.renderParts(skinState.payload(), skinState.models(), modelStack, submitNodeCollector);
		
		viewStack.popMatrix();
	}
	
	protected void renderParts(
		final PlayerDisplayGuiPayload payload,
		final PlayerDisplayGuiModels models,
		final PoseStack modelStack,
		final SubmitNodeCollector submitNodeCollector)
	{
		if(payload.bodyTexture() != null)
		{
			submitNodeCollector.submitModel(
				models.player(),
				models.state(),
				modelStack,
				payload.bodyTexture().texturePath(),
				LIGHT,
				OverlayTexture.NO_OVERLAY,
				0,
				null
			);
		}
		
		if(payload.elytraTextureSupplier() != null)
		{
			this.extractFromSupplierAndRender(
				payload.elytraTextureSupplier(), id ->
				{
					modelStack.translate(0.0f, 0.0f, 0.125f);
					
					submitNodeCollector.submitModel(
						models.elytra(),
						models.state(),
						modelStack,
						RenderTypes.armorCutoutNoCull(id),
						LIGHT,
						OverlayTexture.NO_OVERLAY,
						0,
						null
					);
				});
		}
		else if(payload.capeTextureSupplier() != null)
		{
			this.extractFromSupplierAndRender(
				payload.capeTextureSupplier(), id ->
				{
					modelStack.mulPose(Axis.XP.rotationDegrees(6.0f));
					
					submitNodeCollector.submitModelPart(
						models.cape().getChild("body").getChild("cape"),
						modelStack,
						RenderTypes.armorCutoutNoCull(id),
						LIGHT,
						OverlayTexture.NO_OVERLAY,
						null
					);
				});
		}
	}
	
	protected void extractFromSupplierAndRender(
		final Supplier<ClientAsset.Texture> supplier,
		final Consumer<Identifier> renderer)
	{
		final ClientAsset.Texture textureAsset = supplier.get();
		if(textureAsset == null)
		{
			return;
		}
		
		final Identifier id = textureAsset.texturePath();
		if(id == null)
		{
			return;
		}
		
		renderer.accept(id);
	}
	
	@Override
	protected String getTextureLabel()
	{
		return "player display";
	}
}
