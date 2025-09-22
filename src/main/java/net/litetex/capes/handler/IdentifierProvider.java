package net.litetex.capes.handler;

import net.minecraft.util.Identifier;


public interface IdentifierProvider
{
	// Never null!
	Identifier identifier();
	
	boolean dynamicIdentifier();
}
