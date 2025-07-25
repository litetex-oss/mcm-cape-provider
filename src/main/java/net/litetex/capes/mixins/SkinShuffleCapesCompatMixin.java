package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import dev.imb11.skinshuffle.compat.CapesCompat;
import net.litetex.capes.Capes;
import net.minecraft.client.util.SkinTextures;


// Hijack the mixin for the Capes mod and modify it as needed
@Mixin(value = CapesCompat.class, remap = false)
public class SkinShuffleCapesCompatMixin
{
	@Inject(method = "loadTextures", at = @At("HEAD"), cancellable = true, remap = false)
	private static void loadTextures(
		final GameProfile profile,
		final SkinTextures oldTextures,
		final CallbackInfoReturnable<SkinTextures> cir)
	{
		if(!Capes.instance().overwriteSkinTextures(profile, () -> oldTextures, cir::setReturnValue))
		{
			cir.setReturnValue(oldTextures);
		}
	}
	
	@Inject(method = "getID", at = @At("HEAD"), cancellable = true, remap = false)
	private void getID(final CallbackInfoReturnable<String> cir)
	{
		cir.setReturnValue(Capes.MOD_ID);
	}
}
