package net.litetex.capes.handler.textures;

import java.io.IOException;
import java.util.stream.IntStream;

import net.litetex.capes.handler.AnimatedNativeImageContainer;
import net.minecraft.client.texture.NativeImage;


public class AnimatedSpriteTextureResolver implements TextureResolver
{
	public static final String ID = "sprite";
	
	@Override
	public String id()
	{
		return ID;
	}
	
	@Override
	public boolean animated()
	{
		return true;
	}
	
	@Override
	public ResolvedTextureData resolve(final byte[] imageData, final boolean shouldOnlyResolveFirstFrame)
		throws IOException
	{
		try(final NativeImage img = NativeImage.read(imageData))
		{
			int totalFrames = img.getHeight() / (img.getWidth() / 2);
			if(shouldOnlyResolveFirstFrame)
			{
				totalFrames = Math.min(1, totalFrames);
			}
			
			return new AnimatedResolvedTextureData(IntStream.range(0, totalFrames)
				.mapToObj(currentFrame -> {
					final NativeImage frame = new NativeImage(img.getWidth(), img.getWidth() / 2, true);
					for(int x = 0; x < frame.getWidth(); x++)
					{
						for(int y = 0; y < frame.getHeight(); y++)
						{
							frame.setColorArgb(x, y, img.getColorArgb(x, y + (currentFrame * (img.getWidth() / 2))));
						}
					}
					return frame;
				})
				.map(AnimatedNativeImageContainer::new)
				.toList());
		}
	}
}
