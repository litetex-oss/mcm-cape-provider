package net.litetex.capes.handler;

import net.minecraft.client.texture.NativeImage;


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
