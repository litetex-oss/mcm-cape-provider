package net.litetex.capes.menu.preview;

import java.util.ArrayList;
import java.util.List;

import net.litetex.capes.Capes;
import net.litetex.capes.CapesI18NKeys;
import net.litetex.capes.menu.MainMenuScreen;
import net.litetex.capes.menu.preview.render.DisplayPlayerEntityRenderer;
import net.litetex.capes.menu.preview.render.PlayerPlaceholderEntity;
import net.litetex.capes.provider.CapeProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;


@SuppressWarnings("checkstyle:MagicNumber")
public class PreviewMenuScreen extends MainMenuScreen
{
	private final PlayerPlaceholderEntity entity;
	private long lastRenderTimeMs;
	private final DisplayPlayerEntityRenderer displayPlayerEntityRenderer;
	
	public PreviewMenuScreen(
		final Screen parent,
		final GameOptions gameOptions)
	{
		super(parent, gameOptions);
		
		this.entity = new PlayerPlaceholderEntity(this.capeProvidersForPreview());
		
		final EntityRendererFactory.Context ctx = new EntityRendererFactory.Context(
			MinecraftClient.getInstance().getEntityRenderDispatcher(),
			MinecraftClient.getInstance().getItemModelManager(),
			MinecraftClient.getInstance().getMapRenderer(),
			MinecraftClient.getInstance().getBlockRenderManager(),
			MinecraftClient.getInstance().getResourceManager(),
			MinecraftClient.getInstance().getLoadedEntityModels(),
			new EquipmentModelLoader(),
			MinecraftClient.getInstance().textRenderer
		);
		
		this.displayPlayerEntityRenderer = new DisplayPlayerEntityRenderer(ctx, this.entity.isSlim());
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	@Override
	protected void initSelfMangedDrawableChilds()
	{
		super.initSelfMangedDrawableChilds();
		
		int buttonW = 200;
		
		this.addSelfManagedDrawableChild(ButtonWidget.builder(
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
			.position((this.width / 2) - (buttonW / 2), 60)
			.size(buttonW, 20)
			.build());
		
		buttonW = 100;
		
		this.addSelfManagedDrawableChild(ButtonWidget.builder(
				Text.translatable(CapesI18NKeys.TOGGLE_ELYTRA),
				b -> this.entity.toggleShowElytra())
			.position((this.width / 4) - (buttonW / 2), 120)
			.size(buttonW, 20)
			.build());
		
		this.addSelfManagedDrawableChild(ButtonWidget.builder(
				Text.translatable(CapesI18NKeys.TOGGLE_PLAYER),
				b -> this.entity.toggleShowBody())
			.position((this.width / 4) - (buttonW / 2), 145)
			.size(buttonW, 20)
			.build());
	}
	
	private Text textForCurrentlyDisplayedCapeProvider()
	{
		return Capes.instance().getCapeProviderForSelf()
			.map(CapeProvider::name)
			.map(Text::literal)
			.orElseGet(() -> Text.translatable(CapesI18NKeys.ACTIVATED_PROVIDERS));
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
	public void renderBackground(final DrawContext context, final int mouseX, final int mouseY, final float delta)
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
		final DrawContext context,
		final int x,
		final int y,
		final int size,
		final PlayerPlaceholderEntity entity)
	{
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 1000);
		context.getMatrices().scale(size, size, -size);
		context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0f));
		
		DiffuseLighting.enableGuiShaderLighting();
		
		final VertexConsumerProvider.Immediate immediate =
			MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
		
		this.displayPlayerEntityRenderer.render(entity, 1.0f, context.getMatrices(), immediate, 0xF000F0);
		immediate.draw();
		
		DiffuseLighting.enableGuiDepthLighting();
		
		context.getMatrices().pop();
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
