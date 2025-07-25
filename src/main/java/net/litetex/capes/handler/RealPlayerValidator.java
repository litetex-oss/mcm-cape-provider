package net.litetex.capes.handler;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.util.collections.MaxSizedHashMap;
import net.minecraft.client.MinecraftClient;


public class RealPlayerValidator
{
	private static final Logger LOG = LoggerFactory.getLogger(RealPlayerValidator.class);
	
	private final Map<UUID, Boolean> cache;
	private final boolean useOnlineValidation;
	
	public RealPlayerValidator(final int playerCacheSize, final boolean useOnlineValidation)
	{
		this.cache = Collections.synchronizedMap(new MaxSizedHashMap<>(playerCacheSize));
		this.useOnlineValidation = useOnlineValidation;
	}
	
	public boolean isReal(final GameProfile profile)
	{
		return this.cache.computeIfAbsent(profile.getId(), ignored -> this.checkReal(profile));
	}
	
	private boolean checkReal(final GameProfile profile)
	{
		final ValidityState validityState = this.determineIfInvalid(MinecraftClient.getInstance(), profile);
		
		LOG.debug(
			"Determined that {}/{} is {}a real player: {}",
			profile.getName(),
			profile.getId(),
			validityState.isValid() ? "" : "NOT ",
			validityState.name());
		
		return validityState.isValid();
	}
	
	private ValidityState determineIfInvalid(final MinecraftClient client, final GameProfile profile)
	{
		// The current player is always valid
		if(profile.getId().equals(client.getSession().getUuidOrNull()))
		{
			return ValidityState.SELF;
		}
		// Only valid players have version 4 (random generated)
		// Some servers report players with different versions,
		// however these are ignored as the cape provider can't match them
		if(profile.getId().version() != 4)
		{
			return ValidityState.UUID_INCORRECT_VERSION;
		}
		if(!this.isValidName(profile.getName()))
		{
			return ValidityState.INVALID_NAME;
		}
		if(this.useOnlineValidation && !this.isValidSessionProfile(client, profile.getId()))
		{
			return ValidityState.ONLINE_VALIDATION_FAIL;
		}
		
		return ValidityState.DEFAULT_OK;
	}
	
	enum ValidityState
	{
		SELF(true),
		UUID_INCORRECT_VERSION(false),
		INVALID_NAME(false),
		ONLINE_VALIDATION_FAIL(false),
		DEFAULT_OK(true);
		
		private final boolean valid;
		
		ValidityState(final boolean valid)
		{
			this.valid = valid;
		}
		
		public boolean isValid()
		{
			return this.valid;
		}
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
	
	private boolean isValidSessionProfile(final MinecraftClient client, final UUID id)
	{
		try
		{
			// Check if this is a real player (not a fake one create by a server)
			// Use secure = false to utilize cache
			return client.getSessionService().fetchProfile(id, false) != null;
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to validate player using online services", ex);
			return true;
		}
	}
}
