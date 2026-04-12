package net.litetex.capes.mixins.compat.minecraftcapes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Pseudo
@Mixin(targets = "net/minecraftcapes/player/DownloadManager", remap = false)
public abstract class DownloadManagerMixin
{
	@Inject(method = "prepareDownload*", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	private static void prepareDownload(final CallbackInfo ci)
	{
		ci.cancel();
	}
}
