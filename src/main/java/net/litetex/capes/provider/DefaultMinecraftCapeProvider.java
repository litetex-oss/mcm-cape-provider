package net.litetex.capes.provider;

import java.net.URI;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.minecraft.client.Minecraft;


public class DefaultMinecraftCapeProvider implements CapeProvider
{
	public static final DefaultMinecraftCapeProvider INSTANCE = new DefaultMinecraftCapeProvider();
	
	@Override
	public String id()
	{
		return "default";
	}
	
	@Override
	public String name()
	{
		return "Default / Minecraft";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return null;
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public URI changeCapeUri(final Minecraft client)
	{
		return URI.create("https://www.minecraft.net/en-us/msaprofile/mygames/editskin");
	}
	
	@Override
	public List<AntiFeature> antiFeatures()
	{
		return List.of(
			// Capes cannot be freely created
			AntiFeatures.NO_FREE_CHOICE,
			// Some capes are only available when you executed an indirect microtransaction at a certain point.
			// This includes:
			// * buying/pre-ordering separate games (e.g. Dungeons II or Bugrock Edition)
			// * participating in a payed "experience" (e.g. Moonlight Trail)
			// * participating in 3rd party events (e.g. TwitchCon)
			//
			// Furthermore you are required to play Bugrock edition to unlock capes on Java Edition starting
			// some time in 2025 (see https://mc.wiki/w/Cape#Obtaining) despite:
			// * the capes being market as "for Java & Bedrock Edition"
			// * there being no technical limitation (older capes did not have this requirement)
			// * some players can't play Bugrock because it's not available on their hardware / OS
			//
			// Furthermore it appears that capes are only used for marketing (new content) purposes and
			// no longer for rewarding helping in some way (see bug tracker for examples).
			AntiFeatures.MICROTRANSACTIONS);
	}
}
