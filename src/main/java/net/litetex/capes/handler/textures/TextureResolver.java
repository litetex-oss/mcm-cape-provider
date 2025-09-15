package net.litetex.capes.handler.textures;

import java.io.IOException;
import java.util.Map;

import net.minecraft.client.texture.NativeImage;


public interface TextureResolver
{
	String id();
	
	boolean animated();
	
	ResolvedTextureData resolve(byte[] imageData) throws IOException;
	
	interface ResolvedTextureData
	{
		Boolean hasElytra(); // null = unknown
	}
	
	
	record DefaultResolvedTextureData(
		NativeImage texture,
		Boolean hasElytra
	) implements ResolvedTextureData
	{
	}
	
	
	record AnimatedResolvedTextureData(
		Map<Integer, NativeImage> textures,
		Boolean hasElytra
	) implements ResolvedTextureData
	{
		public AnimatedResolvedTextureData(final Map<Integer, NativeImage> textures)
		{
			this(textures, null);
		}
	}
}
