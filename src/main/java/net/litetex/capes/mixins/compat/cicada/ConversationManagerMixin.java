package net.litetex.capes.mixins.compat.cicada;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// Disable this mind-boggling BS that looks like a backdoor
// It is absolutely not required for mod functionality
@Pseudo
@Mixin(targets = "nl/enjarai/cicada/api/conversation/ConversationManager", remap = false)
public abstract class ConversationManagerMixin
{
	@Inject(method = "init", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	void init(final CallbackInfo ci)
	{
		ci.cancel();
	}
	
	@Inject(method = "load", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	void load(final CallbackInfo ci)
	{
		ci.cancel();
	}
	
	@Inject(method = "run", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
	void run(final CallbackInfo ci)
	{
		ci.cancel();
	}
}
