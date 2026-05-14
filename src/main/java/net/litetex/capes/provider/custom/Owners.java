package net.litetex.capes.provider.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.authlib.GameProfile;

import net.minecraft.util.StringUtil;


public record Owners(
	Set<UUID> uuids,
	Set<String> names)
{
	private static final int UUID_LENGTH = 36;
	
	public boolean owns(final GameProfile gameProfile)
	{
		return this.uuids != null && this.uuids.contains(gameProfile.id())
			|| this.names != null && this.names.contains(gameProfile.name());
	}
	
	public static Owners fromLines(final Logger logger, final List<String> lines)
	{
		final Set<UUID> uuids = new HashSet<>();
		final Set<String> names = new HashSet<>();
		
		lines.stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(s -> !s.isEmpty()
				&& !s.startsWith("#") // Ignore comments
			)
			.map(line -> {
				final int hashTagIndex = line.indexOf('#');
				return hashTagIndex != -1 ? line.substring(0, hashTagIndex).trim() : line;
			})
			.forEach(line -> {
				if(line.length() == UUID_LENGTH)
				{
					try
					{
						uuids.add(UUID.fromString(line));
					}
					catch(final Exception ex)
					{
						logger.warn("Failed to parse UUID={}", line, ex);
					}
				}
				else if(StringUtil.isValidPlayerName(line))
				{
					names.add(line);
				}
			});
		
		return new Owners(uuids, names);
	}
}
