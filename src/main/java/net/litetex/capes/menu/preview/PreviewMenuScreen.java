package net.litetex.capes.menu.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.handler.PlayerCapeHandlerManager;
import net.litetex.capes.handler.TextureProvider;
import net.litetex.capes.i18n.CapesI18NKeys;
import net.litetex.capes.menu.MainMenuScreen;
import net.litetex.capes.menu.preview.render.PlayerDisplayGuiPayload;
import net.litetex.capes.menu.preview.render.PlayerDisplayWidget;
import net.litetex.capes.provider.CapeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;


@SuppressWarnings("checkstyle:MagicNumber")
public class PreviewMenuScreen extends MainMenuScreen
{
	private final PlayerDisplayWidget playerWidget;
	
	private final ViewModel viewModel = new ViewModel();
	
	public PreviewMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions);
		
		final PlayerLimbAnimator playerLimbAnimator = new PlayerLimbAnimator(60);
		this.playerWidget = new PlayerDisplayWidget(
			120,
			120,
			Minecraft.getInstance().getEntityModels(),
			this.viewModel::getPayload,
			models -> playerLimbAnimator.animate(models.player(), 1));
		this.playerWidget.rotationY = 185; // Default view = from behind, facing the cape/elytra
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
					
					this.viewModel.providerChanged();
					
					button.setMessage(this.textForCurrentlyDisplayedCapeProvider());
				})
			.pos((this.width / 2) - (buttonW / 2), 60)
			.size(buttonW, 20)
			.build());
		
		this.playerWidget.setHeight(Math.clamp(this.height - 120, 25, 180));
		this.playerWidget.setPosition(this.width / 2 - this.playerWidget.getWidth() / 2, 82);
		
		buttonW = 100;
		final int playerWidgetCenterY = this.playerWidget.getY() + (this.playerWidget.getHeight() / 2);
		
		this.addSelfManagedDrawableChild(Button.builder(
				Component.translatable(CapesI18NKeys.TOGGLE_ELYTRA),
				b -> this.viewModel.toggleShowElytra())
			.pos((this.width / 4) - (buttonW / 2), playerWidgetCenterY - 23)
			.size(buttonW, 20)
			.build());
		
		this.addSelfManagedDrawableChild(Button.builder(
				Component.translatable(CapesI18NKeys.TOGGLE_PLAYER),
				b -> this.viewModel.toggleShowBody())
			.pos((this.width / 4) - (buttonW / 2), playerWidgetCenterY + 2)
			.size(buttonW, 20)
			.build());
		
		this.addSelfManagedDrawableChild(this.playerWidget);
	}
	
	private Component textForCurrentlyDisplayedCapeProvider()
	{
		return Capes.instance().getCapeProviderForSelf()
			.map(CapeProvider::name)
			.map(Component::literal)
			.orElseGet(() -> Component.translatable(CapesI18NKeys.ACTIVATED_PROVIDERS));
	}
	
	static class ViewModel
	{
		private static final Supplier<ClientAsset.Texture> DEFAULT_ELYTRA_SUPPLIER =
			() -> Capes.DEFAULT_ELYTRA_TEXTURE;
		
		private final GameProfile gameProfile;
		private PlayerSkin skin;
		private boolean slim;
		
		private List<CapeProvider> capeProviders;
		
		private Supplier<ClientAsset.Texture> capeTextureSupplier;
		private Supplier<ClientAsset.Texture> elytraTextureSupplier = DEFAULT_ELYTRA_SUPPLIER;
		
		private boolean showBody = true;
		private boolean showElytra;
		
		private PlayerDisplayGuiPayload payload;
		
		public ViewModel()
		{
			this.gameProfile = Minecraft.getInstance().getGameProfile();
			this.skin = DefaultPlayerSkin.get(this.gameProfile);
			
			this.refreshActiveCapeProviders();
			this.rebuildPayload();
			
			Minecraft.getInstance().getSkinManager().get(this.gameProfile)
				.thenAcceptAsync(optSkinTextures ->
					optSkinTextures.ifPresent(skinTextures -> {
						this.skin = skinTextures;
						this.slim = PlayerModelType.SLIM.equals(this.skin.model());
						
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
					
					final Supplier<ClientAsset.Texture> determinedCapeTextureSupplier =
						this.determineCapeTextureSupplier(handler);
					this.capeTextureSupplier = determinedCapeTextureSupplier;
					
					this.elytraTextureSupplier = handler == null
						|| handler.hasElytraTexture()
						&& Capes.instance().config().isEnableElytraTexture()
						? determinedCapeTextureSupplier
						: DEFAULT_ELYTRA_SUPPLIER;
					
					this.rebuildPayload();
				});
		}
		
		private Supplier<ClientAsset.Texture> determineCapeTextureSupplier(final PlayerCapeHandler handler)
		{
			if(handler != null)
			{
				final TextureProvider textureProvider = handler.capeTextureProvider().orElse(null);
				if(textureProvider != null)
				{
					if(textureProvider.dynamicIdentifier())
					{
						return textureProvider::texture;
					}
					
					// Fetch only once
					final ClientAsset.Texture identifier = textureProvider.texture();
					return () -> identifier;
				}
			}
			
			final Capes capes = Capes.instance();
			final Optional<CapeProvider> provider = capes.getCapeProviderForSelf();
			// Is all active providers and useDefaultProvider?
			return provider.isEmpty() && capes.isUseDefaultProvider()
				// Default provider is present?
				|| provider.filter(Capes.EXCLUDE_DEFAULT_MINECRAFT_CP).isEmpty()
				? this.skin::cape
				: () -> null;
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
				this.showBody ? this.skin.body() : null,
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
		
		public void animate(final PlayerModel player, final float tickDelta)
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
			player.rightArm.xRot = Mth.cos(a + 3.1415927f) * 2.0f * LIMB_DISTANCE * 0.5f;
			player.leftArm.xRot = Mth.cos(a) * 2.0f * LIMB_DISTANCE * 0.5f;
			player.rightLeg.xRot = Mth.cos(a) * 1.4f * LIMB_DISTANCE;
			player.leftLeg.xRot = Mth.cos(a + 3.1415927f) * 1.4f * LIMB_DISTANCE;
		}
	}
}
