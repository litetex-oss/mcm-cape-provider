package net.litetex.capes.util.collections;

import java.util.LinkedHashMap;
import java.util.Map;


public class MaxSizedHashMap<K, V> extends LinkedHashMap<K, V>
{
	private final int maxSize;
	
	public MaxSizedHashMap(final int maxSize)
	{
		this.maxSize = maxSize;
	}
	
	@Override
	protected boolean removeEldestEntry(final Map.Entry<K, V> eldest)
	{
		return this.size() > this.maxSize;
	}
}
