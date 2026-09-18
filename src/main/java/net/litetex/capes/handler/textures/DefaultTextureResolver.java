package net.litetex.capes.handler.textures;

import java.io.IOException;

import com.mojang.blaze3d.platform.NativeImage;


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
	public ResolvedTextureData resolve(final byte[] imageData, final boolean shouldOnlyResolveFirstFrame)
		throws IOException
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
					imgNew.setPixel(x, y, img.getPixel(x, y));
				}
			}
			
			return new DefaultResolvedTextureData(imgNew, Math.floorDiv(srcWidth, srcHeight) == 2);
		}
	}
}
