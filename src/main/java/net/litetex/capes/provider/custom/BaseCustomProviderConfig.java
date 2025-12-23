package net.litetex.capes.provider.custom;

import org.jetbrains.annotations.Nullable;


public interface BaseCustomProviderConfig
{
	String id();
	
	String name();
	
	@Nullable
	Owners owners();
}
