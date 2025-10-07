package net.litetex.capes.mixins.compat.cicada;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;


// Always a great idea to
// 1. Copy-paste some code so that the original mod no longer works
// 2. Have your server get DDOSed by it
// 3. Display some shitty cape that says people should update instead of simply BLOCKING THE REQUESTS
// https://github.com/enjarai/cicada-lib/issues/12
@Pseudo
@Mixin(targets = "nl/enjarai/cicada/util/CapeHandler", remap = false)
public abstract class CapeHandlerMixin
{
	@Inject(method = "onLoadTexture", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void loadTextures(
		final GameProfile profile,
		final CallbackInfo ci)
	{
		ci.cancel();
	}
}
