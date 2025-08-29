package net.litetex.capes.provider;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.authlib.GameProfile;

import net.litetex.capes.provider.antifeature.AntiFeature;
import net.litetex.capes.provider.antifeature.AntiFeatures;
import net.minecraft.client.MinecraftClient;


public class OptiFineCapeProvider implements CapeProvider
{
	private static final Logger LOG = LoggerFactory.getLogger(OptiFineCapeProvider.class);
	
	public static final String ID = "optifine";
	
	@Override
	public String id()
	{
		return ID;
	}
	
	@Override
	public String name()
	{
		return "OptiFine";
	}
	
	@Override
	public String getBaseUrl(final GameProfile profile)
	{
		return "http://s.optifine.net/capes/" + profile.getName() + ".png";
	}
	
	@Override
	public boolean hasChangeCapeUrl()
	{
		return true;
	}
	
	@Override
	public String changeCapeUrl(final MinecraftClient client)
	{
		try
		{
			final BigInteger random1Bi = new BigInteger(128, new Random());
			final BigInteger random2Bi = new BigInteger(128, new Random(System.identityHashCode(new Object())));
			
			final String serverId = random1Bi.xor(random2Bi).toString(16);
			
			final UUID id = client.getGameProfile().getId();
			
			client.getSessionService().joinServer(id, client.getSession().getAccessToken(), serverId);
			return "https://optifine.net/capeChange?"
				+ "u=" + id.toString().replace("-", "")
				+ "&n=" + client.getSession().getUsername()
				+ "&s=" + serverId;
		}
		catch(final Exception ex)
		{
			LOG.warn("Failed to authenticate OF cape editor", ex);
			return null;
		}
	}
	
	@Override
	public String homepageUrl()
	{
		return "https://optifine.net/home";
	}
	
	@Override
	public List<AntiFeature> antiFeatures()
	{
		return List.of(
			AntiFeatures.PAYMENT_TO_UNLOCK_CAPE, // https://optifine.net/donate
			AntiFeatures.BAD_CONNECTION
		);
	}
}
