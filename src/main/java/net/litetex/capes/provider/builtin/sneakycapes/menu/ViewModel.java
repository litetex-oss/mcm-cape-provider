package net.litetex.capes.provider.builtin.sneakycapes.menu;

import java.util.function.Supplier;

import net.litetex.capes.ui.playerpreview.BaseViewModel;
import net.litetex.capes.ui.playerpreview.render.PlayerDisplayGuiPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.player.PlayerSkin;


class ViewModel extends BaseViewModel
{
	private Supplier<ClientAsset.Texture> capeTextureSupplier;
	
	ViewModel()
	{
		super(Minecraft.getInstance().getGameProfile());
		
		this.rebuildPayload();
		
		this.initiallyResolveSkin();
	}
	
	PlayerSkin skin()
	{
		return this.skin;
	}
	
	void updateCapeTextureSupplier(
		final Supplier<ClientAsset.Texture> capeTextureSupplier)
	{
		this.capeTextureSupplier = capeTextureSupplier;
		this.rebuildPayload();
	}
	
	@Override
	protected PlayerDisplayGuiPayload createRebuiltPayload()
	{
		return new PlayerDisplayGuiPayload(
			this.showBody ? this.skin.body() : null,
			this.capeTextureSupplier,
			this.showElytra ? this.capeTextureSupplier : null,
			this.slim
		);
	}
}
