package net.litetex.capes.provider.antifeature;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


public class DefaultAntiFeature implements AntiFeature
{
	private final MutableComponent text;
	
	public DefaultAntiFeature(final String text)
	{
		this(Component.literal(text));
	}
	
	public DefaultAntiFeature(final MutableComponent text)
	{
		this.text = text;
	}
	
	@Override
	public MutableComponent message()
	{
		return this.text;
	}
}
