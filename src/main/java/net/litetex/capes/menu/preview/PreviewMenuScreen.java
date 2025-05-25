package net.litetex.capes.menu.preview;

import java.util.ArrayList;
import java.util.List;

import net.litetex.capes.Capes;
import net.litetex.capes.CapesI18NKeys;
import net.litetex.capes.menu.MainMenuScreen;
import net.litetex.capes.menu.preview.render.PlayerDisplayWidget;
import net.litetex.capes.menu.preview.render.PlayerPlaceholderEntity;
import net.litetex.capes.provider.CapeProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;


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
		
		final PlayerDisplayWidget playerWidget = new PlayerDisplayWidget(
			90,
			125,
			MinecraftClient.getInstance().getLoadedEntityModels(),
			() -> new SkinTextures(
				this.entity.getSkinTexture(),
				null,
				this.entity.getCapeTexture(),
				this.entity.getElytraTexture(),
				this.entity.isSlim() ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE,
				true
			));
		playerWidget.setPosition(this.width / 2 - playerWidget.getWidth() / 2, 82);
		this.addSelfManagedDrawableChild(playerWidget);
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
