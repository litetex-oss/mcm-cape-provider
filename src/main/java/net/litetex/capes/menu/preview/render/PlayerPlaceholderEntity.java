package net.litetex.capes.menu.preview.render;

import java.util.Collection;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.handler.PlayerHandler;
import net.litetex.capes.provider.CapeProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;


public class PlayerPlaceholderEntity
{
	final GameProfile gameProfile;
	Collection<CapeProvider> capeProviders;
	SkinTextures skin;
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
	
	public PlayerPlaceholderEntity(final Collection<CapeProvider> capeProviders)
	{
		this.gameProfile = MinecraftClient.getInstance().getGameProfile();
		this.skin = DefaultSkinHelper.getSkinTextures(this.gameProfile);
		MinecraftClient.getInstance().getSkinProvider().fetchSkinTextures(this.gameProfile)
			.thenAcceptAsync(optSkinTextures ->
				optSkinTextures.ifPresent(skinTextures -> {
					this.skin = skinTextures;
					this.slim = SkinTextures.Model.SLIM.equals(this.skin.model());
				}));
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
			PlayerHandler.onLoadTexture(this.gameProfile, false, this.capeProviders, () -> this.capeLoaded = true);
		}
	}
	
	public Identifier getCapeTexture()
	{
		final PlayerHandler handler = PlayerHandler.getProfile(this.gameProfile);
		return handler != null && handler.hasCape() ? handler.getCape() : this.skin.capeTexture();
	}
	
	public Identifier getElytraTexture()
	{
		final PlayerHandler handler = PlayerHandler.getProfile(this.gameProfile);
		final Identifier capeTexture = this.getCapeTexture();
		return handler == null
			|| (handler.hasElytraTexture()
			&& Capes.instance().config().isEnableElytraTexture()
			&& capeTexture != null)
			? capeTexture
			: Capes.DEFAULT_ELYTRA_IDENTIFIER;
	}
	
	public Identifier getSkinTexture()
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
