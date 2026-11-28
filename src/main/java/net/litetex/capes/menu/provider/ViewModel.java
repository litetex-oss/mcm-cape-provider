package net.litetex.capes.menu.provider;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.litetex.capes.Capes;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.handler.PlayerCapeHandlerManager;
import net.litetex.capes.handler.TextureProvider;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.ui.playerpreview.BaseViewModel;
import net.litetex.capes.ui.playerpreview.render.PlayerDisplayGuiPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;


class ViewModel extends BaseViewModel
{
	private static final Supplier<ClientAsset.Texture> DEFAULT_ELYTRA_SUPPLIER =
		() -> Capes.DEFAULT_ELYTRA_TEXTURE;
	
	private Optional<CapeProvider> optCapeProvider = Optional.empty();
	private List<CapeProvider> capeProviders;
	
	private Supplier<ClientAsset.Texture> capeTextureSupplier;
	private Supplier<ClientAsset.Texture> elytraTextureSupplier = DEFAULT_ELYTRA_SUPPLIER;
	
	ViewModel()
	{
		super(Minecraft.getInstance().getGameProfile());
		
		this.refreshActiveCapeProviders();
		this.rebuildPayload();
		
		this.initiallyResolveSkin(() -> this.updateCapeAndElytraTexture(this.optCapeProvider));
	}
	
	private void refreshActiveCapeProviders()
	{
		this.capeProviders = this.optCapeProvider.map(List::of)
			.orElseGet(() -> Capes.instance().activeCapeProviders());
	}
	
	private void updateCapeAndElytraTexture(final Optional<CapeProvider> optCapeProviderBeforeLoad)
	{
		this.capeTextureSupplier = null;
		this.elytraTextureSupplier = null;
		this.rebuildPayload();
		
		final PlayerCapeHandlerManager playerCapeHandlerManager = Capes.instance().playerCapeHandlerManager();
		playerCapeHandlerManager.onLoadTexture(
			this.gameProfile, false, this.capeProviders, () -> {
				if(optCapeProviderBeforeLoad != this.optCapeProvider)
				{
					// Ignore when the selection was changed in the meantime
					return;
				}
				
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
		// Is all active providers and useDefaultProvider?
		return this.optCapeProvider.isEmpty() && capes.isUseDefaultProvider()
			// Default provider is present?
			|| this.optCapeProvider.filter(Capes.EXCLUDE_DEFAULT_MINECRAFT_CP).isEmpty()
			? this.skin::cape
			: () -> null;
	}
	
	void changeProviderTo(@Nullable final CapeProvider capeProvider)
	{
		final Optional<CapeProvider> inputCapeProvider = Optional.ofNullable(capeProvider);
		this.optCapeProvider = inputCapeProvider;
		this.refreshActiveCapeProviders();
		this.updateCapeAndElytraTexture(inputCapeProvider);
	}
	
	@Override
	protected PlayerDisplayGuiPayload createRebuiltPayload()
	{
		return new PlayerDisplayGuiPayload(
			this.showBody ? this.skin.body() : null,
			this.capeTextureSupplier,
			this.showElytra ? this.elytraTextureSupplier : null,
			this.slim
		);
	}
}
