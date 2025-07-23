package net.litetex.capes.handler;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.Capes;
import net.litetex.capes.util.collections.MaxSizedHashMap;


public class ProfileTextureLoadThrottler
{
	private final Map<UUID, Instant> loadThrottle;
	private final PlayerCapeHandlerManager playerCapeHandlerManager;
	
	public ProfileTextureLoadThrottler(
		final PlayerCapeHandlerManager playerCapeHandlerManager,
		final int playerCacheSize)
	{
		this.playerCapeHandlerManager = playerCapeHandlerManager;
		this.loadThrottle = Collections.synchronizedMap(new MaxSizedHashMap<>(playerCacheSize));
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
