package net.litetex.capes.fabric;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.litetex.capes.Capes;
import net.litetex.capes.config.Config;
import net.litetex.capes.provider.CapeProviders;


public class FabricCapes implements ClientModInitializer
{
	private static final Logger LOG = LoggerFactory.getLogger(FabricCapes.class);
	
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	@Override
	public void onInitializeClient()
	{
		final Config config = this.loadConfig();
		Capes.setInstance(new Capes(
			config,
			this::saveConfig,
			CapeProviders.findAllProviders(config.getCustomProviders())
		));
		
		LOG.debug("Initialized");
	}
	
	private Path configFilePath()
	{
		return FabricLoader.getInstance().getConfigDir().resolve("cape-provider.json5");
	}
	
	private Config loadConfig()
	{
		final Path configFilePath = this.configFilePath();
		if(Files.exists(configFilePath))
		{
			try
			{
				return this.gson.fromJson(Files.readString(configFilePath), Config.class);
			}
			catch(final IOException ioe)
			{
				LOG.warn("Failed to read config file", ioe);
			}
		}
		
		final Config defaultConfig = Config.createDefault();
		this.saveConfig(defaultConfig);
		return defaultConfig;
	}
	
	private void saveConfig(final Config config)
	{
		try
		{
			Files.writeString(
				this.configFilePath(),
				this.gson.toJson(config));
		}
		catch(final IOException ioe)
		{
			throw new UncheckedIOException("Failed to save config", ioe);
		}
	}
}
