package net.litetex.capes.provider.suppliers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.litetex.capes.config.CustomProviderConfig;
import net.litetex.capes.provider.CapeProvider;
import net.litetex.capes.provider.CustomProvider;
import net.litetex.capes.provider.DefaultMinecraftCapeProvider;


public final class CapeProviders
{
	public static Map<String, CapeProvider> findAllProviders(
		final List<CustomProviderConfig> customProviderConfigs,
		final ModMetadataProviderSupplier modMetadataProviderSupplier)
	{
		return Stream.of(
				// Service loading (internal)
				ServiceLoader.load(CapeProvider.class).stream().map(ServiceLoader.Provider::get),
				// User defined custom provider
				customProviderConfigs != null
					? customProviderConfigs.stream()
					.filter(Objects::nonNull)
					.map(CustomProvider::new)
					: null,
				// Mod Metadata provider
				modMetadataProviderSupplier != null ? modMetadataProviderSupplier.get() : null,
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
