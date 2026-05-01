package net.litetex.capes.provider.custom;

import org.jspecify.annotations.Nullable;


public interface BaseCustomProviderConfig
{
	String id();
	
	String name();
	
	@Nullable
	Owners owners();
}
