package net.litetex.capes.fabric;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.litetex.capes.Capes;
import net.litetex.capes.config.Config;
import net.litetex.capes.handler.textures.suppliers.TextureResolvers;
import net.litetex.capes.menu.preview.render.PlayerDisplayGuiElementRenderer;


public class FabricCapes implements ClientModInitializer
{
	private static final Logger LOG = LoggerFactory.getLogger(FabricCapes.class);
	
	private final Gson gson = new GsonBuilder()
		.registerTypeAdapter(Instant.class, new InstantConverter())
		.setPrettyPrinting()
		.create();
	
	@Override
	public void onInitializeClient()
	{
		SpecialGuiElementRegistry.register(ctx -> new PlayerDisplayGuiElementRenderer(ctx.vertexConsumers()));
		
		final Path configDir = FabricLoader.getInstance().getConfigDir().resolve("cape-provider");
		final Path configFile = configDir.resolve("config.json");
		
		Capes.setInstance(new Capes(
			FabricLoader.getInstance().getGameDir().resolve(".mods").resolve("cape-provider"),
			configDir,
			this.loadConfig(configFile),
			config -> this.saveConfig(configFile, config),
			FabricModMetadataProviderSupplier::new,
			TextureResolvers.findAllResolvers()
		));
		
		LOG.debug("Initialized");
	}
	
	private Config loadConfig(final Path configFile)
	{
		boolean configFileExists = Files.exists(configFile);
		if(!configFileExists)
		{
			final Path legacyConfigPath = FabricLoader.getInstance().getConfigDir().resolve("cape-provider.json5");
			if(Files.exists(legacyConfigPath))
			{
				try
				{
					Files.createDirectories(configFile.getParent());
					Files.move(legacyConfigPath, configFile, StandardCopyOption.REPLACE_EXISTING);
					
					configFileExists = true;
					LOG.info("Migrated legacy config file {} -> {}", legacyConfigPath, configFile);
				}
				catch(final IOException e)
				{
					LOG.warn("Failed to move legacy config file", e);
				}
			}
		}
		
		if(configFileExists)
		{
			try
			{
				return this.gson.fromJson(Files.readString(configFile), Config.class);
			}
			catch(final Exception ex)
			{
				LOG.warn("Failed to read config file", ex);
			}
		}
		
		final Config defaultConfig = Config.createDefault();
		this.saveConfig(configFile, defaultConfig);
		return defaultConfig;
	}
	
	private void saveConfig(final Path configFile, final Config config)
	{
		try
		{
			Files.createDirectories(configFile.getParent());
			Files.writeString(
				configFile,
				this.gson.toJson(config));
		}
		catch(final IOException ioe)
		{
			throw new UncheckedIOException("Failed to save config", ioe);
		}
	}
	
	static class InstantConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant>
	{
		public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;
		
		@Override
		public JsonElement serialize(final Instant src, final Type typeOfSrc, final JsonSerializationContext context)
		{
			return new JsonPrimitive(FORMATTER.format(src));
		}
		
		@Override
		public Instant deserialize(
			final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context)
		{
			return FORMATTER.parse(json.getAsString(), Instant::from);
		}
	}
}
