package net.litetex.capes.mixins.skin_body_access;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;

import net.litetex.capes.access.skin_body_access.GameProfileUUIDAccessScope;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.entity.player.PlayerSkin;


@Mixin(SkinManager.class)
public abstract class SkinManagerMixin
{
	@WrapMethod(method = "createLookup")
	Supplier<PlayerSkin> createLookUp(
		final GameProfile profile,
		final boolean requireSecure,
		final Operation<Supplier<PlayerSkin>> original)
	{
		return ScopedValue.where(GameProfileUUIDAccessScope.UUID, profile.id())
			.call(() -> original.call(profile, requireSecure));
	}
}
