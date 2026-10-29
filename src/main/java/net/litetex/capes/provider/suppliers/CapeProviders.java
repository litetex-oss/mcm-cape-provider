package net.litetex.capes.provider.suppliers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.DefaultMinecraftCapeProvider;
import net.litetex.capes.provider.custom.local.LocalCustomProvider;
import net.litetex.capes.provider.custom.remote.RemoteCustomProvider;
import net.litetex.capes.provider.custom.remote.RemoteCustomProviderConfig;


public final class CapeProviders
{
	public static Map<String, CapeProvider> findAllProviders(
		final List<RemoteCustomProviderConfig> remoteCustomProviderConfigs,
		final CompletableFuture<Set<LocalCustomProvider>> cfSimpleFileSystemProviders,
		final CompletableFuture<Set<CapeProvider>> cfModMetadataProviders)
	{
		return Stream.of(
				// Service loading (internal)
				ServiceLoader.load(CapeProvider.class).stream().map(ServiceLoader.Provider::get),
				// User defined custom provider in config.json
				remoteCustomProviderConfigs != null
					? remoteCustomProviderConfigs.stream()
					.filter(Objects::nonNull)
					.map(RemoteCustomProvider::new)
					: null,
				// SimpleFileSystemProviders
				cfSimpleFileSystemProviders.join().stream(),
				// Mod Metadata provider
				cfModMetadataProviders.join().stream(),
				// Default
				Stream.of(DefaultMinecraftCapeProvider.INSTANCE))
			.filter(Objects::nonNull)
			.flatMap(s -> s)
			.map(CapeProvider.class::cast)
			// Use LinkedHashMap to keep order
			.collect(Collectors.toMap(
				CapeProvider::id,
				Function.identity(),
				(e1, e2) -> e2,
				LinkedHashMap::new));
	}
	
	private CapeProviders()
	{
	}
}
