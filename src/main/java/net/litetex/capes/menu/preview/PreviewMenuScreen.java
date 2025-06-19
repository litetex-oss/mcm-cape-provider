package net.litetex.capes.menu.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.CapesI18NKeys;
import net.litetex.capes.handler.IdentifierProvider;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.handler.PlayerCapeHandlerManager;
import net.litetex.capes.menu.MainMenuScreen;
import net.litetex.capes.menu.preview.render.PlayerDisplayGuiPayload;
import net.litetex.capes.menu.preview.render.PlayerDisplayWidget;
import net.litetex.capes.provider.CapeProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;


@SuppressWarnings("checkstyle:MagicNumber")
public class PreviewMenuScreen extends MainMenuScreen
{
	private final PlayerDisplayWidget playerWidget;
	
	private final ViewModel viewModel = new ViewModel();
	
	public PreviewMenuScreen(
		final Screen parent,
		final GameOptions gameOptions)
	{
		super(parent, gameOptions);
		
		final PlayerLimbAnimator playerLimbAnimator = new PlayerLimbAnimator(60);
		this.playerWidget = new PlayerDisplayWidget(
			120,
			120,
			MinecraftClient.getInstance().getLoadedEntityModels(),
			this.viewModel::getPayload,
			models -> playerLimbAnimator.animate(models.player(), 1));
		this.playerWidget.yRotation = 185; // Default view = from behind, facing the cape/elytra
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
					
					this.viewModel.providerChanged();
					
					button.setMessage(this.textForCurrentlyDisplayedCapeProvider());
				})
			.position((this.width / 2) - (buttonW / 2), 60)
			.size(buttonW, 20)
			.build());
		
		this.playerWidget.setHeight(Math.clamp(this.height - 120, 25, 180));
		this.playerWidget.setPosition(this.width / 2 - this.playerWidget.getWidth() / 2, 82);
		
		buttonW = 100;
		final int playerWidgetCenterY = this.playerWidget.getY() + (this.playerWidget.getHeight() / 2);
		
		this.addSelfManagedDrawableChild(ButtonWidget.builder(
				Text.translatable(CapesI18NKeys.TOGGLE_ELYTRA),
				b -> this.viewModel.toggleShowElytra())
			.position((this.width / 4) - (buttonW / 2), playerWidgetCenterY - 23)
			.size(buttonW, 20)
			.build());
		
		this.addSelfManagedDrawableChild(ButtonWidget.builder(
				Text.translatable(CapesI18NKeys.TOGGLE_PLAYER),
				b -> this.viewModel.toggleShowBody())
			.position((this.width / 4) - (buttonW / 2), playerWidgetCenterY + 2)
			.size(buttonW, 20)
			.build());
		
		this.addSelfManagedDrawableChild(this.playerWidget);
	}
	
	private Text textForCurrentlyDisplayedCapeProvider()
	{
		return Capes.instance().getCapeProviderForSelf()
			.map(CapeProvider::name)
			.map(Text::literal)
			.orElseGet(() -> Text.translatable(CapesI18NKeys.ACTIVATED_PROVIDERS));
	}
	
	static class ViewModel
	{
		private static final Supplier<Identifier> DEFAULT_ELYTRA_SUPPLIER = () -> Capes.DEFAULT_ELYTRA_IDENTIFIER;
		
		private final GameProfile gameProfile;
		private SkinTextures skin;
		private boolean slim;
		
		private List<CapeProvider> capeProviders;
		
		private Supplier<Identifier> capeTextureSupplier;
		private Supplier<Identifier> elytraTextureSupplier = DEFAULT_ELYTRA_SUPPLIER;
		
		private boolean showBody = true;
		private boolean showElytra;
		
		private PlayerDisplayGuiPayload payload;
		
		public ViewModel()
		{
			this.gameProfile = MinecraftClient.getInstance().getGameProfile();
			this.skin = DefaultSkinHelper.getSkinTextures(this.gameProfile);
			
			this.refreshActiveCapeProviders();
			this.rebuildPayload();
			
			MinecraftClient.getInstance().getSkinProvider().fetchSkinTextures(this.gameProfile)
				.thenAcceptAsync(optSkinTextures ->
					optSkinTextures.ifPresent(skinTextures -> {
						this.skin = skinTextures;
						this.slim = SkinTextures.Model.SLIM.equals(this.skin.model());
						
						this.updateCapeAndElytraTexture();
					}));
		}
		
		private void refreshActiveCapeProviders()
		{
			final Capes capes = Capes.instance();
			this.capeProviders = capes.getCapeProviderForSelf()
				.map(List::of)
				.orElseGet(capes::activeCapeProviders);
		}
		
		private void updateCapeAndElytraTexture()
		{
			this.capeTextureSupplier = null;
			this.elytraTextureSupplier = null;
			this.rebuildPayload();
			
			final PlayerCapeHandlerManager playerCapeHandlerManager = Capes.instance().playerCapeHandlerManager();
			playerCapeHandlerManager.onLoadTexture(
				this.gameProfile, false, this.capeProviders, () -> {
					final PlayerCapeHandler handler = playerCapeHandlerManager.getProfile(this.gameProfile);
					
					final Supplier<Identifier> determinedCapeTextureSupplier =
						this.determineCapeIdentifierSupplier(handler);
					this.capeTextureSupplier = determinedCapeTextureSupplier;
					
					this.elytraTextureSupplier = handler == null
						|| handler.hasElytraTexture()
						&& Capes.instance().config().isEnableElytraTexture()
						? determinedCapeTextureSupplier
						: DEFAULT_ELYTRA_SUPPLIER;
					
					this.rebuildPayload();
				});
		}
		
		private Supplier<Identifier> determineCapeIdentifierSupplier(final PlayerCapeHandler handler)
		{
			if(handler != null)
			{
				final IdentifierProvider identifierProvider = handler.capeIdentifierProvider().orElse(null);
				if(identifierProvider != null)
				{
					if(identifierProvider.dynamicIdentifier())
					{
						return identifierProvider::identifier;
					}
					
					// Fetch only once
					final Identifier identifier = identifierProvider.identifier();
					return () -> identifier;
				}
			}
			return this.skin::capeTexture;
		}
		
		public void providerChanged()
		{
			this.refreshActiveCapeProviders();
			this.updateCapeAndElytraTexture();
		}
		
		public void toggleShowBody()
		{
			this.showBody = !this.showBody;
			this.rebuildPayload();
		}
		
		public void toggleShowElytra()
		{
			this.showElytra = !this.showElytra;
			this.rebuildPayload();
		}
		
		private void rebuildPayload()
		{
			this.payload = new PlayerDisplayGuiPayload(
				this.showBody ? this.skin.texture() : null,
				this.capeTextureSupplier,
				this.showElytra ? this.elytraTextureSupplier : null,
				this.slim
			);
		}
		
		public PlayerDisplayGuiPayload getPayload()
		{
			return this.payload;
		}
	}
	
	
	static class PlayerLimbAnimator
	{
		private static final float LIMB_DISTANCE = -0.1f;
		private final int msBetweenUpdates;
		private long nextUpdateTimeMs;
		
		private float limbAngle;
		
		public PlayerLimbAnimator(final int fps)
		{
			this.msBetweenUpdates = 1000 / fps;
		}
		
		public void animate(final PlayerEntityModel player, final float tickDelta)
		{
			if(player == null)
			{
				return;
			}
			
			final long currentTimeMs = System.currentTimeMillis();
			if(currentTimeMs > this.nextUpdateTimeMs)
			{
				this.nextUpdateTimeMs = currentTimeMs + this.msBetweenUpdates;
				
				this.limbAngle += LIMB_DISTANCE;
			}
			
			final float calcLimbAngle = this.limbAngle - LIMB_DISTANCE * (1.0f - tickDelta);
			
			final float a = calcLimbAngle * 0.6662f;
			player.rightArm.pitch = MathHelper.cos(a + 3.1415927f) * 2.0f * LIMB_DISTANCE * 0.5f;
			player.leftArm.pitch = MathHelper.cos(a) * 2.0f * LIMB_DISTANCE * 0.5f;
			player.rightLeg.pitch = MathHelper.cos(a) * 1.4f * LIMB_DISTANCE;
			player.leftLeg.pitch = MathHelper.cos(a + 3.1415927f) * 1.4f * LIMB_DISTANCE;
		}
	}
}
