package net.litetex.capes.handler.textures;

import java.io.IOException;
import java.util.List;

import net.litetex.capes.handler.AnimatedNativeImageContainer;
import net.minecraft.client.texture.NativeImage;


public interface TextureResolver
{
	String id();
	
	boolean animated();
	
	ResolvedTextureData resolve(byte[] imageData, boolean shouldOnlyResolveFirstFrame) throws IOException;
	
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
		List<AnimatedNativeImageContainer> textures,
		Boolean hasElytra
	) implements ResolvedTextureData
	{
		public AnimatedResolvedTextureData(final List<AnimatedNativeImageContainer> textures)
		{
			this(textures, null);
		}
	}
}
