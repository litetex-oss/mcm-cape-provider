package net.litetex.capes.util.collections;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;


public class DiscardingQueue<E> extends LinkedBlockingQueue<E>
{
	private final Consumer<E> onDiscarded;
	
	public DiscardingQueue(final int capacity, final Consumer<E> onDiscarded)
	{
		super(capacity);
		this.onDiscarded = onDiscarded;
	}
	
	@Override
	public boolean offer(@NotNull final E e)
	{
		if (this.remainingCapacity() == 0)
		{
			final E discarded = this.poll();
			if(discarded != null)
			{
				this.onDiscarded.accept(discarded);
			}
		}
		return super.offer(e);
	}
}
