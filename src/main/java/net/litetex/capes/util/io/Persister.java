package net.litetex.capes.util.io;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;

import net.litetex.capes.util.json.JSONSerializer;


public final class Persister
{
	public static <T> Optional<T> tryRead(final Logger logger, final Path path, final Class<T> clazz)
	{
		try
		{
			final long startMs = System.currentTimeMillis();
			
			final Optional<T> optContent =
				Optional.ofNullable(JSONSerializer.GSON.fromJson(Files.readString(path), clazz));
			
			logger.debug("Reading {} took {}ms", path, System.currentTimeMillis() - startMs);
			return optContent;
		}
		catch(final NoSuchFileException nsfe)
		{
			return Optional.empty();
		}
		catch(final Exception ex)
		{
			logger.warn("Failed to read {}", path, ex);
			return Optional.empty();
		}
	}
	
	public static <T> boolean trySave(final Logger logger, final Path path, final Supplier<T> value)
	{
		try
		{
			final long startMs = System.currentTimeMillis();
			
			Files.createDirectories(path.getParent());
			Files.writeString(path, JSONSerializer.GSON.toJson(value.get()));
			
			logger.debug("Saving {} took {}ms", path, System.currentTimeMillis() - startMs);
			return true;
		}
		catch(final Exception ex)
		{
			logger.warn("Failed to save {}", path, ex);
			return false;
		}
	}
	
	public static boolean tryDelete(final Logger logger, final Path path)
	{
		try
		{
			return Files.deleteIfExists(path);
		}
		catch(final Exception ex)
		{
			logger.warn("Failed to delete {}", path, ex);
			return false;
		}
	}
	
	private Persister()
	{
	}
}
