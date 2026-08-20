package net.litetex.capes.handler;

import com.mojang.blaze3d.platform.NativeImage;


public record AnimatedNativeImageContainer(
	NativeImage image,
	int delayMs
)
{
	public AnimatedNativeImageContainer(final NativeImage image)
	{
		this(image, 100);
	}
}
