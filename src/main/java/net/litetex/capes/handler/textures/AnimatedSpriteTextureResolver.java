package net.litetex.capes.handler.textures;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
	public ResolvedTextureData resolve(final byte[] imageData) throws IOException
	{
		final Map<Integer, NativeImage> frames = new HashMap<>();
		try(final NativeImage img = NativeImage.read(imageData))
		{
			final int totalFrames = img.getHeight() / (img.getWidth() / 2);
			for(int currentFrame = 0; currentFrame < totalFrames; currentFrame++)
			{
				final NativeImage frame = new NativeImage(img.getWidth(), img.getWidth() / 2, true);
				for(int x = 0; x < frame.getWidth(); x++)
				{
					for(int y = 0; y < frame.getHeight(); y++)
					{
						frame.setColorArgb(x, y, img.getColorArgb(x, y + (currentFrame * (img.getWidth() / 2))));
					}
				}
				frames.put(currentFrame, frame);
			}
		}
		return new AnimatedResolvedTextureData(frames);
	}
}
