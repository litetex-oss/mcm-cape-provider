package net.litetex.capes.util.sync;

import java.util.function.Consumer;
import java.util.function.Function;


public class SynchronizedContainer<T>
{
	private final Object lock;
	private final T value;
	
	public SynchronizedContainer(final T value)
	{
		this(new Object(), value);
	}
	
	protected SynchronizedContainer(final Object lock, final T value)
	{
		this.lock = lock;
		this.value = value;
	}
	
	public T value()
	{
		return this.value;
	}
	
	public void execWithLock(final Consumer<T> consumer)
	{
		synchronized(this.lock)
		{
			consumer.accept(this.value);
		}
	}
	
	public <R> R supplyWithLock(final Function<T, R> func)
	{
		synchronized(this.lock)
		{
			return func.apply(this.value);
		}
	}
}
