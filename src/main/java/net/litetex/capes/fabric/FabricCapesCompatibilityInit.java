package net.litetex.capes.fabric;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class FabricCapesCompatibilityInit
{
	private static final Logger LOG = LoggerFactory.getLogger(FabricCapesCompatibilityInit.class);
	
	public static void init()
	{
		if(FabricModDetector.isFabricRenderingApiPresent())
		{
			run("FabricRenderingApiInitializer");
		}
	}
	
	private static void run(final String clazzName)
	{
		try
		{
			((Runnable)Class.forName("net.litetex.capes.fabric.compat." + clazzName).getConstructor().newInstance())
				.run();
			LOG.debug("Initialized compat: {}", clazzName);
		}
		catch(final InstantiationException
					| IllegalAccessException
					| InvocationTargetException
					| NoSuchMethodException
					| ClassNotFoundException e)
		{
			throw new RuntimeException("Failed to init compat for " + clazzName, e);
		}
	}
	
	private FabricCapesCompatibilityInit()
	{
	}
}
