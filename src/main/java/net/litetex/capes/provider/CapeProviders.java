package net.litetex.capes.provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.litetex.capes.config.CustomProviderConfig;


public final class CapeProviders
{
	public static Map<String, CapeProvider> findAllProviders(final List<CustomProviderConfig> customProviderConfigs)
	{
		final ServiceLoader<CapeProvider> loader = ServiceLoader.load(CapeProvider.class);
		return Stream.concat(
				loader.stream()
					.map(ServiceLoader.Provider::get),
				Stream.concat(
					customProviderConfigs != null
						? customProviderConfigs.stream()
						.filter(Objects::nonNull)
						.map(CustomProvider::new)
						: Stream.empty(),
					// Always load default provider
					Stream.of(DefaultMinecraftCapeProvider.INSTANCE))
			)
			// Use LinkedHashMap to keep order
			.collect(Collectors.toMap(CapeProvider::id, Function.identity(), (e1, e2) -> e2, LinkedHashMap::new));
	}
	
	private CapeProviders()
	{
	}
}
