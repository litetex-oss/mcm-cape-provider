package net.litetex.capes.fabric;

import net.fabricmc.loader.api.FabricLoader;


public final class FabricModDetector
{
	public static boolean isFabricRenderingApiPresent()
	{
		return FabricLoader.getInstance().isModLoaded("fabric-renderer-api-v1");
	}
	
	private FabricModDetector()
	{
	}
}
