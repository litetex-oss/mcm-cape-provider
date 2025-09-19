package net.litetex.capes.mixins.compat.skinshuffle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.minecraft.client.util.SkinTextures;


// Hijack the mixin for the Capes mod and modify it as needed
@Pseudo
@Mixin(targets = "dev/imb11/skinshuffle/compat/CapesCompat", remap = false)
public abstract class CapesCompatMixin
{
	@Inject(method = "loadTextures", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
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
	
	@Inject(method = "getID", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private void getID(final CallbackInfoReturnable<String> cir)
	{
		cir.setReturnValue(Capes.MOD_ID);
	}
}
