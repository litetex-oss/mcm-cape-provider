package net.litetex.capes.provider.suppliers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.litetex.capes.provider.custom.Owners;
import net.litetex.capes.provider.custom.local.LocalCustomProvider;
import net.litetex.capes.provider.custom.local.LocalCustomProviderConfig;


public final class SimpleFilesystemProviders
{
	private static final Logger LOG = LoggerFactory.getLogger(SimpleFilesystemProviders.class);
	
	public static final String CAPE_FILE_NAME = "cape.png";
	public static final String OWNER_FILE_NAME = "owners.txt";
	
	public static Set<LocalCustomProvider> createFsProviders(final Path modConfigDir)
	{
		final Map<String, Path> providersPaths = new LinkedHashMap<>(Map.of("Default", modConfigDir));
		
		final Path simpleCustomDir = modConfigDir.resolve("simple-custom");
		if(Files.exists(simpleCustomDir))
		{
			try(final Stream<Path> stream = Files.list(simpleCustomDir))
			{
				stream
					.filter(Files::isDirectory)
					.forEach(path -> providersPaths.put(path.getFileName().toString(), path));
			}
			catch(final IOException ioe)
			{
				LOG.warn("Failed to list simpleCustomDir", ioe);
			}
		}
		
		return providersPaths.entrySet()
			.stream()
			.filter(e -> e.getKey().length() < 200
				&& (e.getKey().isEmpty() || StringUtils.isAlphanumeric(e.getKey())))
			.map(e -> {
				
				if(!Files.exists(e.getValue()))
				{
					return null;
				}
				
				final String provider = e.getKey();
				return tryCreate(
					"local-simple-fs-" + provider.toLowerCase(),
					provider,
					e.getValue());
			})
			.filter(Objects::nonNull)
			.map(LocalCustomProvider::new)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}
	
	public static LocalCustomProviderConfig tryCreate(
		final String id,
		final String name,
		final Path path)
	{
		final Path capeFile = path.resolve(CAPE_FILE_NAME);
		if(!Files.exists(capeFile))
		{
			return null;
		}
		
		final byte[] capeTexture;
		try
		{
			capeTexture = Files.readAllBytes(capeFile);
		}
		catch(final IOException e)
		{
			LOG.warn("Failed to read cape texture from {}", capeFile, e);
			return null;
		}
		
		return new LocalCustomProviderConfig(
			id,
			getNameOverride(path).orElse(name) + " (Local)",
			getOwners(path),
			capeTexture
		);
	}
	
	private static Optional<String> getNameOverride(final Path path)
	{
		final Path nameFile = path.resolve("name.txt");
		if(Files.exists(nameFile))
		{
			try
			{
				return Files.readAllLines(nameFile)
					.stream()
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.findFirst();
			}
			catch(final Exception e)
			{
				LOG.warn("Failed to read name from {}", nameFile, e);
			}
		}
		return Optional.empty();
	}
	
	private static Owners getOwners(final Path path)
	{
		final Path ownersFile = path.resolve(OWNER_FILE_NAME);
		if(Files.exists(ownersFile))
		{
			try
			{
				return Owners.fromLines(LOG, Files.readAllLines(ownersFile));
			}
			catch(final Exception e)
			{
				LOG.warn("Failed to read owners from {}", ownersFile, e);
			}
		}
		return null;
	}
	
	private SimpleFilesystemProviders()
	{
	}
}
