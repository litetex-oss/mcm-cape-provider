package net.litetex.capes.fabric;

import net.fabricmc.loader.api.FabricLoader;


public final class FabricDetector
{
	public static boolean isRenderingApiPresent()
	{
		return FabricLoader.getInstance().isModLoaded("fabric-renderer-api-v1");
	}
	
	private FabricDetector()
	{
	}
}
