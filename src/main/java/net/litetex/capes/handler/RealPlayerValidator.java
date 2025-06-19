package net.litetex.capes.handler;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;


public class RealPlayerValidator
{
	private static final Logger LOG = LoggerFactory.getLogger(RealPlayerValidator.class);
	
	private final Map<UUID, Boolean> cache = new ConcurrentHashMap<>(new WeakHashMap<>());
	
	public boolean isReal(final GameProfile profile)
	{
		return this.cache.computeIfAbsent(profile.getId(), ignored -> this.checkReal(profile));
	}
	
	private boolean checkReal(final GameProfile profile)
	{
		final MinecraftClient client = MinecraftClient.getInstance();
		
		// The current player is always valid
		final boolean real = profile.getId().equals(client.getSession().getUuidOrNull())
			// Shortcut: Check if the name is valid
			|| this.isValidName(profile.getName())
			// Check if this is a real player (not a fake one create by a server)
			// Use secure = false to utilize cache
			&& client.getSessionService().fetchProfile(profile.getId(), false) != null;
		
		LOG.debug(
			"Determined that {}/{} is {}a real player",
			profile.getName(),
			profile.getId(),
			real ? "" : "NOT ");
		
		return real;
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	private boolean isValidName(final String playerName)
	{
		final int length = playerName.length();
		if(length < 3 || length > 16)
		{
			return false;
		}
		
		for(int i = 0; i < length; i++)
		{
			final char c = playerName.charAt(i);
			if(!(c >= 'a' && c <= 'z'
				|| c >= 'A' && c <= 'Z'
				|| c >= '0' && c <= '9'
				|| c == '_'))
			{
				return false;
			}
		}
		return true;
	}
}
