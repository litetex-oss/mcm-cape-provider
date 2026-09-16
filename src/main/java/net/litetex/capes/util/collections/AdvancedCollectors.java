package net.litetex.capes.util.collections;

import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;


public final class AdvancedCollectors
{
	public static <T, K, V> Collector<T, ?, LinkedHashMap<K, V>> toLinkedHashMap(
		final Function<T, K> keyMapper,
		final Function<T, V> valueMapper)
	{
		return Collectors.toMap(
			keyMapper,
			valueMapper,
			(l, r) -> r,
			LinkedHashMap::new);
	}
	
	private AdvancedCollectors()
	{
	}
}
