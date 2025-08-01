package net.litetex.capes.fabric;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.litetex.capes.Capes;
import net.litetex.capes.config.Config;
import net.litetex.capes.menu.preview.render.PlayerDisplayGuiElementRenderer;
import net.litetex.capes.provider.suppliers.CapeProviders;


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
		
		final Config config = this.loadConfig();
		Capes.setInstance(new Capes(
			config,
			this::saveConfig,
			CapeProviders.findAllProviders(
				config.getCustomProviders(),
				config.getModProviderHandling().load() ? new FabricModMetadataProviderSupplier() : null)
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
			throws JsonParseException
		{
			return FORMATTER.parse(json.getAsString(), Instant::from);
		}
	}
}
