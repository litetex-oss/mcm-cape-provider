package net.litetex.capes.menu.preview.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;


@SuppressWarnings("checkstyle:MagicNumber")
public class DisplayPlayerEntityRenderer
	extends LivingEntityRenderer<LivingEntity, PlayerEntityRenderState, PlayerEntityModel>
{
	private final EntityRendererFactory.Context ctx;
	
	public DisplayPlayerEntityRenderer(
		final EntityRendererFactory.Context ctx,
		final boolean slim)
	{
		super(
			ctx,
			new PlayerEntityModel(
				ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER),
				slim),
			0.5f);
		this.ctx = ctx;
	}
	
	public void render(
		final PlayerPlaceholderEntity livingEntity,
		final float tickDelta,
		final MatrixStack matrixStack,
		final VertexConsumerProvider vertexConsumerProvider,
		final int light)
	{
		this.setModelPose();
		matrixStack.push();
		
		matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - livingEntity.yaw));
		matrixStack.scale(-1.0f, -1.0f, 1.0f);
		matrixStack.translate(0.0f, -1.501f, 0.0f);
		
		float limbDistance = MathHelper.lerp(tickDelta, livingEntity.lastLimbDistance, livingEntity.limbDistance);
		final float limbAngle = livingEntity.limbAngle - livingEntity.limbDistance * (1.0f - tickDelta);
		
		if(limbDistance > 1.0f)
		{
			limbDistance = 1.0f;
		}
		
		this.setAngles(limbAngle, limbDistance);
		
		if(livingEntity.showBody)
		{
			final VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
				this.model.getLayer(livingEntity.getSkinTexture()));
			final int overlay = OverlayTexture.packUv(OverlayTexture.getU(0f), OverlayTexture.getV(false));
			this.model.render(matrixStack, vertexConsumer, light, overlay);
		}
		
		this.renderCapeOrElytra(livingEntity, matrixStack, vertexConsumerProvider, light);
		
		matrixStack.pop();
	}
	
	private void renderCapeOrElytra(
		final PlayerPlaceholderEntity livingEntity,
		final MatrixStack matrixStack,
		final VertexConsumerProvider vertexConsumerProvider,
		final int light)
	{
		if(!livingEntity.capeLoaded)
		{
			livingEntity.loadCapeTextureIfRequired();
			return;
		}
		if(livingEntity.showElytra)
		{
			final Identifier identifier = livingEntity.getElytraTexture();
			matrixStack.push();
			matrixStack.translate(0.0f, 0.0f, 0.125f);
			
			final VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(
				vertexConsumerProvider,
				RenderLayer.getArmorCutoutNoCull(identifier),
				false);
			new ElytraEntityModel(this.ctx.getEntityModels().getModelPart(EntityModelLayers.ELYTRA))
				.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
			matrixStack.pop();
			return;
		}
		
		if(livingEntity.getCapeTexture() == null)
		{
			return;
		}
		
		matrixStack.push();
		matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0f));
		final VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
			RenderLayer.getArmorCutoutNoCull(livingEntity.getCapeTexture()));
		this.ctx.getPart(EntityModelLayers.PLAYER_CAPE)
			.getChild("body")
			.getChild("cape")
			.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
		matrixStack.pop();
	}
	
	private void setAngles(final float f, final float g)
	{
		this.model.body.yaw = 0.0f;
		this.model.rightArm.originZ = 0.0f;
		this.model.rightArm.originX = -5.0f;
		this.model.leftArm.originZ = 0.0f;
		this.model.leftArm.originX = 5.0f;
		this.model.rightArm.pitch = MathHelper.cos(f * 0.6662f + 3.1415927f) * 2.0f * g * 0.5f;
		this.model.leftArm.pitch = MathHelper.cos(f * 0.6662f) * 2.0f * g * 0.5f;
		this.model.rightArm.roll = 0.0f;
		this.model.leftArm.roll = 0.0f;
		this.model.rightLeg.pitch = MathHelper.cos(f * 0.6662f) * 1.4f * g;
		this.model.leftLeg.pitch = MathHelper.cos(f * 0.6662f + 3.1415927f) * 1.4f * g;
		this.model.rightLeg.yaw = 0.0f;
		this.model.leftLeg.yaw = 0.0f;
		this.model.rightLeg.roll = 0.0f;
		this.model.leftLeg.roll = 0.0f;
		this.model.rightArm.yaw = 0.0f;
		this.model.leftArm.yaw = 0.0f;
		this.model.body.pitch = 0.0f;
		this.model.rightLeg.originZ = 0.1f;
		this.model.leftLeg.originZ = 0.1f;
		this.model.rightLeg.originY = 12.0f;
		this.model.leftLeg.originY = 12.0f;
		this.model.head.originY = 0.0f;
		this.model.body.originY = 0.0f;
		this.model.leftArm.originY = 2.0f;
		this.model.rightArm.originY = 2.0f;
	}
	
	private void setModelPose()
	{
		final GameOptions options = MinecraftClient.getInstance().options;
		final PlayerEntityModel playerEntityModel = this.getModel();
		playerEntityModel.setVisible(true);
		playerEntityModel.hat.visible = options.isPlayerModelPartEnabled(PlayerModelPart.HAT);
		playerEntityModel.jacket.visible = options.isPlayerModelPartEnabled(PlayerModelPart.JACKET);
		playerEntityModel.leftPants.visible = options.isPlayerModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
		playerEntityModel.rightPants.visible = options.isPlayerModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
		playerEntityModel.leftSleeve.visible = options.isPlayerModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
		playerEntityModel.rightSleeve.visible = options.isPlayerModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);
	}
	
	@Override
	public Identifier getTexture(final PlayerEntityRenderState state)
	{
		return DefaultSkinHelper.getTexture();
	}
	
	@Override
	public PlayerEntityRenderState createRenderState()
	{
		return new PlayerEntityRenderState();
	}
}
