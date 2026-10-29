package net.litetex.capes.mixins;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.minecraft.client.Minecraft;
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
		if(!Capes.instance().config().isOnlyLoadForSelf() || Minecraft.getInstance().isLocalPlayer(profile.id()))
		{
			Capes.instance().textureLoadThrottler().loadIfRequired(profile);
		}
	}
	
	@WrapMethod(
		method = "getSkin",
		order = 1042 // suppress actions of other mods if present
	)
	private PlayerSkin getSkin(final Operation<PlayerSkin> original)
	{
		return Capes.instance().applySkinTexture(this.profile, original.call());
	}
	
	@Shadow
	@Final
	private GameProfile profile;
}
