package net.litetex.capes.mixins.compat.skinshuffle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.minecraft.world.entity.player.PlayerSkin;


// Hijack the mixin for the Capes mod and modify it as needed
@Pseudo
@Mixin(targets = "dev/imb11/skinshuffle/compat/CapesCompat", remap = false)
public abstract class CapesCompatMixin
{
	@WrapMethod(
		method = "loadTextures",
		order = 942,
		remap = false,
		require = 0)
	private static PlayerSkin loadTextures(
		final GameProfile profile,
		final PlayerSkin textures,
		final Operation<PlayerSkin> original)
	{
		return Capes.instance().applySkinTexture(profile, textures);
	}
	
	@Inject(method = "getID", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private void getID(final CallbackInfoReturnable<String> cir)
	{
		cir.setReturnValue(Capes.MOD_ID);
	}
}
