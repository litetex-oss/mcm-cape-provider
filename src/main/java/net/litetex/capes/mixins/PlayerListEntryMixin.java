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
import net.litetex.capes.util.GameProfileUtil;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;


@Mixin(PlayerInfo.class)
public abstract class PlayerListEntryMixin
{
	@Inject(method = "createSkinLookup", at = @At("HEAD"))
	private static void loadTextures(
		final GameProfile profile,
		final CallbackInfoReturnable<Supplier<PlayerSkin>> cir)
	{
		if(!Capes.instance().config().isOnlyLoadForSelf() || GameProfileUtil.isSelf(profile))
		{
			Capes.instance().textureLoadThrottler().loadIfRequired(profile);
		}
	}
	
	@Inject(
		method = "getSkin",
		at = @At("TAIL"),
		order = 1001, // Slightly later to suppress actions of other mods if present
		cancellable = true)
	private void getCapeTexture(final CallbackInfoReturnable<PlayerSkin> cir)
	{
		Capes.instance().overwriteSkinTextures(this.profile, cir::getReturnValue, cir::setReturnValue);
	}
	
	@Shadow
	@Final
	private GameProfile profile;
}
