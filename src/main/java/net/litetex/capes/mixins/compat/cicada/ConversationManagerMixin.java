package net.litetex.capes.mixins.compat.cicada;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import nl.enjarai.cicada.api.conversation.ConversationManager;


// Disable this mind-boggling BS that looks like a backdoor
// It is absolutely not required for mod functionality
@Mixin(value = ConversationManager.class, remap = false)
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
