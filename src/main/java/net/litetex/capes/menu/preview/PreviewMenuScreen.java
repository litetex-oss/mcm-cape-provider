package net.litetex.capes.menu.preview;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;

import net.litetex.capes.Capes;
import net.litetex.capes.menu.MainMenuScreen;
import net.litetex.capes.menu.preview.render.DisplayPlayerEntityRenderer;
import net.litetex.capes.menu.preview.render.PlayerPlaceholderEntity;
import net.litetex.capes.provider.CapeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;


@SuppressWarnings("checkstyle:MagicNumber")
public class PreviewMenuScreen extends MainMenuScreen
{
	private final PlayerPlaceholderEntity entity;
	private long lastRenderTimeMs;
	
	public PreviewMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions);
		this.entity = new PlayerPlaceholderEntity(
			this.capeProvidersForPreview(),
			this.capes().playerCapeHandlerManager());
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	@Override
	protected void initSelfMangedDrawableChilds()
	{
		super.initSelfMangedDrawableChilds();
		
		int buttonW = 200;
		
		this.addSelfManagedDrawableChild(Button.builder(
				this.textForCurrentlyDisplayedCapeProvider(),
				button -> {
					final Capes capes = Capes.instance();
					
					final List<CapeProvider> providers = new ArrayList<>(capes.getAllProviders().values());
					final int nextIndex = capes.getCapeProviderForSelf().map(providers::indexOf).orElse(-1) + 1;
					
					capes.config().setCurrentPreviewProviderId(
						nextIndex > providers.size() - 1
							? null
							: providers.get(nextIndex % providers.size()).id());
					capes.saveConfig();
					
					this.entity.forceCapeRefresh(this.capeProvidersForPreview());
					
					button.setMessage(this.textForCurrentlyDisplayedCapeProvider());
				})
			.pos((this.width / 2) - (buttonW / 2), 60)
			.size(buttonW, 20)
			.build());
		
		buttonW = 100;
		
		this.addSelfManagedDrawableChild(Button.builder(
				Component.literal("Toggle Elytra"),
				b -> this.entity.toggleShowElytra())
			.pos((this.width / 4) - (buttonW / 2), 120)
			.size(buttonW, 20)
			.build());
		
		this.addSelfManagedDrawableChild(Button.builder(
				Component.literal("Toggle Player"),
				b -> this.entity.toggleShowBody())
			.pos((this.width / 4) - (buttonW / 2), 145)
			.size(buttonW, 20)
			.build());
	}
	
	private Component textForCurrentlyDisplayedCapeProvider()
	{
		return Capes.instance().getCapeProviderForSelf()
			.map(CapeProvider::name)
			.map(Component::literal)
			.orElseGet(() -> Component.literal("Activated Providers"));
	}
	
	private List<CapeProvider> capeProvidersForPreview()
	{
		final Capes capes = Capes.instance();
		return capes.getCapeProviderForSelf()
			.map(List::of)
			.orElseGet(capes::activeCapeProviders);
	}
	
	// See also InventoryScreen
	@Override
	public void renderBackground(final GuiGraphics context, final int mouseX, final int mouseY, final float delta)
	{
		super.renderBackground(context, mouseX, mouseY, delta);
		
		final int playerX = this.width / 2;
		final int playerY = 204;
		
		final long currentTimeMs = System.currentTimeMillis();
		
		if(currentTimeMs > this.lastRenderTimeMs + (1000 / 60))
		{
			this.lastRenderTimeMs = currentTimeMs;
			this.entity.updatePrevX();
			this.entity.updateLimbs();
		}
		
		this.drawPlayer(context, playerX, playerY, 64, this.entity);
	}
	
	void drawPlayer(
		final GuiGraphics context,
		final int x,
		final int y,
		final int size,
		final PlayerPlaceholderEntity entity)
	{
		context.pose().pushPose();
		context.pose().translate(x, y, 1000);
		context.pose().scale(size, size, -size);
		context.pose().mulPose(Axis.ZP.rotationDegrees(180.0f));
		
		Lighting.setupForEntityInInventory();
		
		final MultiBufferSource.BufferSource immediate =
			Minecraft.getInstance().renderBuffers().bufferSource();
		final EntityRendererProvider.Context ctx = new EntityRendererProvider.Context(
			Minecraft.getInstance().getEntityRenderDispatcher(),
			Minecraft.getInstance().getItemRenderer(),
			Minecraft.getInstance().getBlockRenderer(),
			Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer(),
			Minecraft.getInstance().getResourceManager(),
			Minecraft.getInstance().getEntityModels(),
			Minecraft.getInstance().font
		);
		
		final DisplayPlayerEntityRenderer displayPlayerEntityRenderer =
			new DisplayPlayerEntityRenderer(ctx, entity.isSlim());
		
		displayPlayerEntityRenderer.render(entity, 1.0f, context.pose(), immediate, 0xF000F0);
		immediate.endBatch();
		
		Lighting.setupFor3DItems();
		
		context.pose().popPose();
	}
	
	@Override
	public boolean mouseDragged(
		final double mouseX,
		final double mouseY,
		final int button,
		final double deltaX,
		final double deltaY)
	{
		super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		this.entity.updateYawDueToMouseDrag((float)deltaX);
		return true;
	}
}
