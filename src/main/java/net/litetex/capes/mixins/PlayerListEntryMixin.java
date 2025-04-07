package net.litetex.capes.mixins;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.handler.PlayerCapeHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;


@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin
{
	@Unique
	private static final Map<UUID, Instant> LOAD_THROTTLE = Collections.synchronizedMap(new WeakHashMap<>());
	
	@Inject(method = "texturesSupplier", at = @At("HEAD"))
	private static void loadTextures(
		final GameProfile profile,
		final CallbackInfoReturnable<Supplier<SkinTextures>> cir)
	{
		final UUID id = profile.getId();
		
		final Instant lastLoadTime = LOAD_THROTTLE.get(id);
		final Instant now = Instant.now();
		if(lastLoadTime == null || lastLoadTime.isBefore(now.minus(Capes.instance().loadThrottleSuppressDuration())))
		{
			LOAD_THROTTLE.put(id, now);
			PlayerCapeHandler.onLoadTexture(profile);
		}
	}
	
	@Inject(method = "getSkinTextures", at = @At("TAIL"), cancellable = true)
	private void getCapeTexture(final CallbackInfoReturnable<SkinTextures> cir)
	{
		final PlayerCapeHandler handler = PlayerCapeHandler.getProfile(this.profile);
		if(handler != null && handler.hasCape())
		{
			final SkinTextures oldTextures = cir.getReturnValue();
			final Identifier capeTexture = handler.getCape();
			final Identifier elytraTexture = handler.hasElytraTexture()
				&& Capes.instance().config().isEnableElytraTexture()
				? capeTexture
				: Capes.DEFAULT_ELYTRA_IDENTIFIER;
			cir.setReturnValue(new SkinTextures(
				oldTextures.texture(),
				oldTextures.textureUrl(),
				capeTexture,
				elytraTexture,
				oldTextures.model(),
				oldTextures.secure()));
		}
	}
	
	@Shadow
	@Final
	private GameProfile profile;
}
