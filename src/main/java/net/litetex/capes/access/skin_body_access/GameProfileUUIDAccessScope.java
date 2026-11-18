package net.litetex.capes.access.skin_body_access;

import java.util.UUID;


public final class GameProfileUUIDAccessScope
{
	public static final ScopedValue<UUID> UUID = ScopedValue.newInstance();
	
	public static UUID getOrNull()
	{
		return UUID.isBound() ? UUID.get() : null;
	}
	
	private GameProfileUUIDAccessScope()
	{
	}
}
