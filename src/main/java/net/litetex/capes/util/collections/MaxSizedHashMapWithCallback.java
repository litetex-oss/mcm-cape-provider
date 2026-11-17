package net.litetex.capes.util.collections;

import java.util.Map;
import java.util.function.Consumer;


public class MaxSizedHashMapWithCallback<K, V> extends MaxSizedHashMap<K, V>
{
	private final Consumer<Map.Entry<K, V>> onRemovedEldest;
	
	public MaxSizedHashMapWithCallback(final int maxSize, final Consumer<Map.Entry<K, V>> onRemovedEldest)
	{
		super(maxSize);
		this.onRemovedEldest = onRemovedEldest;
	}
	
	@Override
	protected boolean removeEldestEntry(final Map.Entry<K, V> eldest)
	{
		final boolean removed = super.removeEldestEntry(eldest);
		if(removed)
		{
			this.onRemovedEldest.accept(eldest);
		}
		return removed;
	}
}
