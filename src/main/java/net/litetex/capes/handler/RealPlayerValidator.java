package net.litetex.capes.handler;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.util.collections.MaxSizedHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtil;


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
		return this.cache.computeIfAbsent(profile.id(), ignored -> this.checkReal(profile));
	}
	
	private boolean checkReal(final GameProfile profile)
	{
		final ValidityState validityState = this.determineIfInvalid(Minecraft.getInstance(), profile);
		
		LOG.debug(
			"Determined that {}/{} is {}a real player: {}",
			profile.name(),
			profile.id(),
			validityState.isValid() ? "" : "NOT ",
			validityState.name());
		
		return validityState.isValid();
	}
	
	private ValidityState determineIfInvalid(final Minecraft client, final GameProfile profile)
	{
		// The current player is always valid
		if(profile.id().equals(client.getUser().getProfileId()))
		{
			return ValidityState.SELF;
		}
		// Only valid players have version 4 (random generated)
		// Some servers report players with different versions,
		// however these are ignored as the cape provider can't match them
		if(profile.id().version() != 4)
		{
			return ValidityState.UUID_INCORRECT_VERSION;
		}
		if(!StringUtil.isValidPlayerName(profile.name()))
		{
			return ValidityState.INVALID_NAME;
		}
		if(this.useOnlineValidation && !this.isValidSessionProfile(client, profile.id()))
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
	
	private boolean isValidSessionProfile(final Minecraft client, final UUID id)
	{
		try
		{
			// Check if this is a real player (not a fake one create by a server)
			// Use secure = false to utilize cache
			return client.services().sessionService().fetchProfile(id, false) != null;
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to validate player using online services", ex);
			return true;
		}
	}
}
