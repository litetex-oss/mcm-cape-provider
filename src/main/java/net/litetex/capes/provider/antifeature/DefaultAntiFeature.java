package net.litetex.capes.provider.antifeature;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;


public class DefaultAntiFeature implements AntiFeature
{
	private final MutableText text;
	
	public DefaultAntiFeature(final String translateKey)
	{
		this(Text.translatable(translateKey));
	}
	
	public DefaultAntiFeature(final MutableText text)
	{
		this.text = text;
	}
	
	@Override
	public MutableText message()
	{
		return this.text;
	}
}
