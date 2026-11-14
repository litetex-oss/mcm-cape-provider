package net.litetex.capes.mixins;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.renderer.texture.SkinTextureDownloader;


@Mixin(SkinTextureDownloader.class)
public abstract class SkinTextureDownloaderMixin
{
	// Minecraft removes the alpha from some unused areas
	// These areas are however used by 3rd party applications like Sneaky Capes
	@Unique
	private static final Map<Integer, List<Rect>> Y_0_REPLACEMENT_RECTS = Map.of(
		// 0, 0, 32, 16
		0, List.of(
			new Rect(8, 0, 24, 8),
			new Rect(0, 8, 32, 16)
		),
		// 0, 16, 64, 32
		16, List.of(
			new Rect(4, 16, 12, 20),
			new Rect(20, 16, 36, 20),
			new Rect(44, 16, 52, 20),
			new Rect(0, 20, 56, 32)
		),
		// 16, 48, 48, 64
		48, List.of(
			new Rect(20, 48, 28, 52),
			new Rect(36, 48, 44, 52),
			new Rect(16, 52, 48, 64)
		)
	);
	
	@WrapOperation(
		method = "processLegacySkin",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;setNoAlpha"
				+ "(Lcom/mojang/blaze3d/platform/NativeImage;IIII)V"
		)
	)
	private static void setNoAlpha(
		final NativeImage image,
		final int x0,
		final int y0,
		final int x1,
		final int y1,
		final Operation<Void> original)
	{
		final List<Rect> rects = Y_0_REPLACEMENT_RECTS.get(y0);
		if(rects == null)
		{
			// Should never happen
			original.call(image, x0, y0, x1, y1);
			return;
		}
		
		rects.forEach(r -> original.call(image, r.x0(), r.y0(), r.x1(), r.y1()));
	}
	
	record Rect(int x0, int y0, int x1, int y1)
	{
	}
}
