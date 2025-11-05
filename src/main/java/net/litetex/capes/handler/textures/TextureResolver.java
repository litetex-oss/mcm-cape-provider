package net.litetex.capes.handler.textures;

import java.io.IOException;
import java.util.List;

import com.mojang.blaze3d.platform.NativeImage;

import net.litetex.capes.handler.AnimatedNativeImageContainer;


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
