package net.litetex.capes.menu.preview.render;

import java.util.Collection;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.handler.PlayerCapeHandlerManager;
import net.litetex.capes.provider.CapeProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;


@SuppressWarnings("checkstyle:MagicNumber")
public class PlayerPlaceholderEntity
{
	final PlayerCapeHandlerManager playerCapeHandlerManager;
	final GameProfile gameProfile;
	Collection<CapeProvider> capeProviders;
	PlayerSkin skin;
	boolean slim;
	boolean showBody = true;
	boolean showElytra;
	boolean capeLoading;
	boolean capeLoaded;
	float limbDistance;
	float lastLimbDistance;
	float limbAngle;
	float yaw;
	float preYaw;
	double x;
	double prevX;
	
	public PlayerPlaceholderEntity(
		final Collection<CapeProvider> capeProviders,
		final PlayerCapeHandlerManager playerCapeHandlerManager)
	{
		this.playerCapeHandlerManager = playerCapeHandlerManager;
		this.gameProfile = Minecraft.getInstance().getGameProfile();
		this.skin = DefaultPlayerSkin.get(this.gameProfile);
		Minecraft.getInstance().getSkinManager().getOrLoad(this.gameProfile)
			.thenAcceptAsync(skinTextures -> {
				this.skin = skinTextures;
				this.slim = PlayerSkin.Model.SLIM.equals(this.skin.model());
			});
		this.forceCapeRefresh(capeProviders);
	}
	
	public void updateLimbs()
	{
		this.lastLimbDistance = this.limbDistance;
		float g = (float)(this.x - this.prevX) * 4.0f;
		if(g > 1.0f)
		{
			g = 1.0f;
		}
		this.limbDistance += (g - this.limbDistance) * 0.4f;
		this.limbAngle += this.limbDistance;
	}
	
	public void loadCapeTextureIfRequired()
	{
		if(!this.capeLoading)
		{
			this.capeLoading = true;
			this.playerCapeHandlerManager.onLoadTexture(
				this.gameProfile,
				false,
				this.capeProviders,
				() -> this.capeLoaded = true);
		}
	}
	
	public ResourceLocation getCapeTexture()
	{
		final PlayerCapeHandler handler = this.playerCapeHandlerManager.getProfile(this.gameProfile);
		return handler != null && handler.hasElytraTexture() ? handler.getCape() : this.skin.capeTexture();
	}
	
	public ResourceLocation getElytraTexture()
	{
		final PlayerCapeHandler handler = this.playerCapeHandlerManager.getProfile(this.gameProfile);
		final ResourceLocation capeTexture = this.getCapeTexture();
		return handler == null
			||
			handler.hasElytraTexture()
				&& Capes.instance().config().isEnableElytraTexture()
				&& capeTexture != null
			? capeTexture
			: Capes.DEFAULT_ELYTRA_TEXTURE;
	}
	
	public ResourceLocation getSkinTexture()
	{
		return this.skin.texture();
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	public void updatePrevX()
	{
		this.prevX = this.x + 0.025;
	}
	
	public boolean isSlim()
	{
		return this.slim;
	}
	
	public void updateYawDueToMouseDrag(final float deltaX)
	{
		this.preYaw = this.yaw;
		this.yaw -= deltaX;
	}
	
	public void toggleShowBody()
	{
		this.showBody = !this.showBody;
	}
	
	public void toggleShowElytra()
	{
		this.showElytra = !this.showElytra;
	}
	
	public void forceCapeRefresh(final Collection<CapeProvider> capeProviders)
	{
		this.capeProviders = capeProviders;
		this.capeLoaded = false;
		this.capeLoading = false;
	}
}
