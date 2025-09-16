package net.litetex.capes.handler.textures;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import net.minecraft.client.texture.NativeImage;


public class AnimatedGIFTextureResolver implements TextureResolver
{
	public static final String ID = "gif";
	
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
		final ImageReader reader = ImageIO.getImageReadersBySuffix("GIF").next();
		try(final ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(imageData)))
		{
			reader.setInput(in);
			int numImages = reader.getNumImages(true);
			if(shouldOnlyResolveFirstFrame)
			{
				numImages = Math.min(1, numImages);
			}
			
			final Map<Integer, NativeImage> frames = new HashMap<>();
			int targetWidth = -1;
			for(int i = 0; i < numImages; i++)
			{
				final BufferedImage img = reader.read(i);
				
				final int width = img.getWidth();
				final int height = img.getHeight();
				
				if(targetWidth == -1) // Only the initial image has the correct width
				{
					targetWidth = img.getWidth();
				}
				
				final NativeImage frame = new NativeImage(targetWidth, targetWidth / 2, true);
				for(int x = 0; x < frame.getWidth(); x++)
				{
					for(int y = 0; y < frame.getHeight(); y++)
					{
						frame.setColorArgb(
							x,
							y,
							// If out of bound due to compression -> transparent
							y >= height || x >= width ? 0x00000000 : img.getRGB(x, y));
					}
				}
				frames.put(i, frame);
			}
			
			return new AnimatedResolvedTextureData(frames);
		}
	}
}
