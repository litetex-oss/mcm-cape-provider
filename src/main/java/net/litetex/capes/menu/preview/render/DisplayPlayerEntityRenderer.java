package net.litetex.capes.menu.preview.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;


@SuppressWarnings("checkstyle:MagicNumber")
public class DisplayPlayerEntityRenderer
	extends LivingEntityRenderer<LivingEntity, PlayerModel<LivingEntity>>
{
	private final EntityRendererProvider.Context ctx;
	
	public DisplayPlayerEntityRenderer(
		final EntityRendererProvider.Context ctx,
		final boolean slim)
	{
		super(
			ctx,
			new PlayerModel<>(
				ctx.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER),
				slim),
			0.5f);
		this.ctx = ctx;
	}
	
	public void render(
		final PlayerPlaceholderEntity livingEntity,
		final float tickDelta,
		final PoseStack matrixStack,
		final MultiBufferSource vertexConsumerProvider,
		final int light)
	{
		this.setModelPose();
		matrixStack.pushPose();
		
		matrixStack.scale(0.9375f, 0.9375f, 0.9375f);
		matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f - livingEntity.yaw));
		matrixStack.scale(-1.0f, -1.0f, 1.0f);
		matrixStack.translate(0.0f, -1.501f, 0.0f);
		
		float limbDistance = Mth.lerp(tickDelta, livingEntity.lastLimbDistance, livingEntity.limbDistance);
		final float limbAngle = livingEntity.limbAngle - livingEntity.limbDistance * (1.0f - tickDelta);
		
		if(limbDistance > 1.0f)
		{
			limbDistance = 1.0f;
		}
		
		this.setAngles(limbAngle, limbDistance);
		
		if(livingEntity.showBody)
		{
			final VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
				this.model.renderType(livingEntity.getSkinTexture()));
			final int overlay = OverlayTexture.pack(OverlayTexture.u(0f), OverlayTexture.v(false));
			this.model.renderToBuffer(matrixStack, vertexConsumer, light, overlay);
		}
		
		this.renderCapeOrElytra(livingEntity, matrixStack, vertexConsumerProvider, light);
		
		matrixStack.popPose();
	}
	
	private void renderCapeOrElytra(
		final PlayerPlaceholderEntity livingEntity,
		final PoseStack matrixStack,
		final MultiBufferSource vertexConsumerProvider,
		final int light)
	{
		if(!livingEntity.capeLoaded)
		{
			livingEntity.loadCapeTextureIfRequired();
			return;
		}
		if(livingEntity.showElytra)
		{
			final ResourceLocation identifier = livingEntity.getElytraTexture();
			matrixStack.pushPose();
			matrixStack.translate(0.0f, 0.0f, 0.125f);
			
			final VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
				vertexConsumerProvider,
				RenderType.armorCutoutNoCull(identifier),
				false);
			patchNotYoung(new ElytraModel<>(this.ctx.getModelSet().bakeLayer(ModelLayers.ELYTRA)))
				.renderToBuffer(matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
			matrixStack.popPose();
			return;
		}
		
		if(livingEntity.getCapeTexture() == null)
		{
			return;
		}
		
		matrixStack.pushPose();
		matrixStack.translate(0.0f, 0.0f, 0.125f);
		matrixStack.mulPose(Axis.XP.rotationDegrees(3.0f));
		matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
		final VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(
			RenderType.armorCutoutNoCull(livingEntity.getCapeTexture()));
		this.ctx.bakeLayer(ModelLayers.PLAYER)
			.getChild("cloak")
			.render(matrixStack, vertexConsumer, light, OverlayTexture.NO_OVERLAY);
		matrixStack.popPose();
	}
	
	private void setAngles(final float f, final float g)
	{
		this.model.body.yRot = 0.0f;
		this.model.rightArm.z = 0.0f;
		this.model.rightArm.x = -5.0f;
		this.model.leftArm.z = 0.0f;
		this.model.leftArm.x = 5.0f;
		this.model.rightArm.xRot = Mth.cos(f * 0.6662f + 3.1415927f) * 2.0f * g * 0.5f;
		this.model.leftArm.xRot = Mth.cos(f * 0.6662f) * 2.0f * g * 0.5f;
		this.model.rightArm.zRot = 0.0f;
		this.model.leftArm.zRot = 0.0f;
		this.model.rightLeg.xRot = Mth.cos(f * 0.6662f) * 1.4f * g;
		this.model.leftLeg.xRot = Mth.cos(f * 0.6662f + 3.1415927f) * 1.4f * g;
		this.model.rightLeg.yRot = 0.0f;
		this.model.leftLeg.yRot = 0.0f;
		this.model.rightLeg.zRot = 0.0f;
		this.model.leftLeg.zRot = 0.0f;
		this.model.rightArm.yRot = 0.0f;
		this.model.leftArm.yRot = 0.0f;
		this.model.body.xRot = 0.0f;
		this.model.rightLeg.z = 0.1f;
		this.model.leftLeg.z = 0.1f;
		this.model.rightLeg.y = 12.0f;
		this.model.leftLeg.y = 12.0f;
		this.model.head.y = 0.0f;
		this.model.body.y = 0.0f;
		this.model.leftArm.y = 2.0f;
		this.model.rightArm.y = 2.0f;
	}
	
	private void setModelPose()
	{
		final Options options = Minecraft.getInstance().options;
		final var playerEntityModel = patchNotYoung(this.getModel());
		playerEntityModel.setAllVisible(true);
		playerEntityModel.hat.visible = options.isModelPartEnabled(PlayerModelPart.HAT);
		playerEntityModel.jacket.visible = options.isModelPartEnabled(PlayerModelPart.JACKET);
		playerEntityModel.leftPants.visible = options.isModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
		playerEntityModel.rightPants.visible = options.isModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
		playerEntityModel.leftSleeve.visible = options.isModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
		playerEntityModel.rightSleeve.visible = options.isModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);
	}
	
	@Override
	public ResourceLocation getTextureLocation(final LivingEntity entity)
	{
		return DefaultPlayerSkin.getDefaultTexture();
	}
	
	private static <M extends EntityModel<?>> M patchNotYoung(final M model)
	{
		model.young = false;
		return model;
	}
}
