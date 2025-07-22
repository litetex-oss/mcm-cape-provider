package net.litetex.capes.fabric;

import net.fabricmc.loader.api.FabricLoader;


public final class FabricModDetector
{
	public static boolean isSkinShufflePresent()
	{
		return FabricLoader.getInstance().isModLoaded("skinshuffle");
	}
	
	private FabricModDetector()
	{
	}
}
