package net.litetex.capes.mixins.compat.minecraftcapes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Pseudo
@Mixin(targets = "net/minecraftcapes/config/MinecraftCapesConfig", remap = false)
public abstract class MinecraftCapesConfigMixin
{
	@Inject(method = "isCapeVisible", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void isCapeVisible(final CallbackInfoReturnable<Boolean> cir)
	{
		cir.setReturnValue(false);
	}
}
