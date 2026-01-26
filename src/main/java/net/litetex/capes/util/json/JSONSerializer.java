package net.litetex.capes.util.json;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public final class JSONSerializer
{
	public static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(Instant.class, new InstantConverter())
		.setPrettyPrinting()
		.create();
	
	private JSONSerializer()
	{
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
