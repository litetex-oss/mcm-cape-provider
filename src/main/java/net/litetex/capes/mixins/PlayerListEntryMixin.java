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
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;


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
	
	@Inject(
		method = "getSkinTextures",
		at = @At("TAIL"),
		order = 1001, // Slightly later to suppress actions of other mods if present
		cancellable = true)
	private void getCapeTexture(final CallbackInfoReturnable<SkinTextures> cir)
	{
		Capes.instance().overwriteSkinTextures(this.profile, cir::getReturnValue, cir::setReturnValue);
	}
	
	@Shadow
	@Final
	private GameProfile profile;
}
