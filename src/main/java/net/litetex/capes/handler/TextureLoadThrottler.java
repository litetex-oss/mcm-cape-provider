package net.litetex.capes.handler;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;


public class TextureLoadThrottler
{
	private final Map<UUID, Instant> loadThrottle = Collections.synchronizedMap(new WeakHashMap<>());
	private final PlayerCapeHandlerManager playerCapeHandlerManager;
	
	public TextureLoadThrottler(final PlayerCapeHandlerManager playerCapeHandlerManager)
	{
		this.playerCapeHandlerManager = playerCapeHandlerManager;
	}
	
	public void loadIfRequired(final GameProfile profile)
	{
		final UUID id = profile.getId();
		final Instant lastLoadTime = this.loadThrottle.get(id);
		final Instant now = Instant.now();
		if(lastLoadTime == null || lastLoadTime.isBefore(now.minus(Capes.instance().loadThrottleSuppressDuration())))
		{
			this.loadThrottle.put(id, now);
			this.playerCapeHandlerManager.onLoadTexture(profile);
		}
	}
	
	public void clearCache()
	{
		this.loadThrottle.clear();
	}
}
