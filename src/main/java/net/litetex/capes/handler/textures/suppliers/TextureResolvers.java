package net.litetex.capes.handler.textures.suppliers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.litetex.capes.handler.textures.DefaultTextureResolver;
import net.litetex.capes.handler.textures.TextureResolver;


public final class TextureResolvers
{
	public static Map<String, TextureResolver> findAllResolvers()
	{
		return Stream.of(
				// Service loading (internal)
				ServiceLoader.load(TextureResolver.class).stream().map(ServiceLoader.Provider::get),
				// Default
				Stream.<TextureResolver>of(DefaultTextureResolver.INSTANCE))
			.flatMap(Function.identity())
			// Use LinkedHashMap to keep order
			.collect(Collectors.toMap(
				TextureResolver::id,
				Function.identity(),
				(e1, e2) -> e2,
				LinkedHashMap::new));
	}
	
	private TextureResolvers()
	{
	}
}
