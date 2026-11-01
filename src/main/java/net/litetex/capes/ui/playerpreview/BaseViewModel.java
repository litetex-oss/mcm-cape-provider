package net.litetex.capes.ui.playerpreview;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.ui.playerpreview.render.PlayerDisplayGuiPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;


public abstract class BaseViewModel implements ViewModel
{
	protected final GameProfile gameProfile;
	protected PlayerSkin skin;
	protected boolean slim;
	
	protected boolean showBody = true;
	protected boolean showElytra;
	
	protected PlayerDisplayGuiPayload payload;
	
	public BaseViewModel(final GameProfile gameProfile)
	{
		this.gameProfile = gameProfile;
		this.skin = DefaultPlayerSkin.get(this.gameProfile);
	}
	
	protected void initiallyResolveSkin()
	{
		this.initiallyResolveSkin(this::rebuildPayload);
	}
	
	protected void initiallyResolveSkin(final Runnable onResolved)
	{
		Minecraft.getInstance().getSkinManager().get(this.gameProfile)
			.thenAcceptAsync(optSkinTextures ->
				optSkinTextures.ifPresent(skinTextures -> {
					this.skin = skinTextures;
					this.slim = PlayerModelType.SLIM.equals(this.skin.model());
					
					onResolved.run();
				}));
	}
	
	@Override
	public void toggleShowBody()
	{
		this.showBody = !this.showBody;
		this.rebuildPayload();
	}
	
	@Override
	public void toggleShowElytra()
	{
		this.showElytra = !this.showElytra;
		this.rebuildPayload();
	}
	
	protected void rebuildPayload()
	{
		this.payload = this.createRebuiltPayload();
	}
	
	protected abstract PlayerDisplayGuiPayload createRebuiltPayload();
	
	@Override
	public PlayerDisplayGuiPayload getPayload()
	{
		return this.payload;
	}
}
