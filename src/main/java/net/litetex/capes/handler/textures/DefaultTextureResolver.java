package net.litetex.capes.handler.textures;

import java.io.IOException;

import net.minecraft.client.texture.NativeImage;


public class DefaultTextureResolver implements TextureResolver
{
	public static final DefaultTextureResolver INSTANCE = new DefaultTextureResolver();
	
	@Override
	public String id()
	{
		return "default";
	}
	
	@Override
	public boolean animated()
	{
		return false;
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	@Override
	public ResolvedTextureData resolve(final byte[] imageData) throws IOException
	{
		try(final NativeImage img = NativeImage.read(imageData))
		{
			int imageWidth = 64;
			int imageHeight = 32;
			final int srcWidth = img.getWidth();
			final int srcHeight = img.getHeight();
			while(imageWidth < srcWidth || imageHeight < srcHeight)
			{
				imageWidth *= 2;
				imageHeight *= 2;
			}
			final NativeImage imgNew = new NativeImage(imageWidth, imageHeight, true);
			for(int x = 0; x < srcWidth; x++)
			{
				for(int y = 0; y < srcHeight; y++)
				{
					imgNew.setColorArgb(x, y, img.getColorArgb(x, y));
				}
			}
			
			return new DefaultResolvedTextureData(imgNew, Math.floorDiv(srcWidth, srcHeight) == 2);
		}
	}
}
