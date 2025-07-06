package net.litetex.capes.mixins;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.litetex.capes.util.GameProfileUtil;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;


@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin
{
	@Inject(method = "texturesSupplier", at = @At("HEAD"))
	private static void loadTextures(
		final GameProfile profile,
		final CallbackInfoReturnable<Supplier<SkinTextures>> cir)
	{
		if(!Capes.instance().config().isOnlyLoadForSelf() || GameProfileUtil.isSelf(profile))
		{
			Capes.instance().textureLoadThrottler().loadIfRequired(profile);
		}
	}
	
	@Inject(method = "getSkinTextures", at = @At("TAIL"), cancellable = true)
	private void getCapeTexture(final CallbackInfoReturnable<SkinTextures> cir)
	{
		final PlayerCapeHandler handler = Capes.instance().playerCapeHandlerManager().getProfile(this.profile);
		if(handler != null)
		{
			final Identifier capeTexture = handler.getCape();
			if(capeTexture != null)
			{
				final SkinTextures oldTextures = cir.getReturnValue();
				final Identifier elytraTexture = handler.hasElytraTexture()
					&& Capes.instance().config().isEnableElytraTexture()
					? capeTexture
					: Capes.DEFAULT_ELYTRA_IDENTIFIER;
				cir.setReturnValue(new SkinTextures(
					oldTextures.texture(),
					oldTextures.textureUrl(),
					capeTexture,
					elytraTexture,
					oldTextures.model(),
					oldTextures.secure()));
			}
		}
	}
	
	@Shadow
	@Final
	private GameProfile profile;
}
