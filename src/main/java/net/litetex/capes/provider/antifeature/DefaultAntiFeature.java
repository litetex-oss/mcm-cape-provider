package net.litetex.capes.provider.antifeature;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


public class DefaultAntiFeature implements AntiFeature
{
	private final MutableComponent text;
	
	public DefaultAntiFeature(final String translateKey)
	{
		this(Component.translatable(translateKey));
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
