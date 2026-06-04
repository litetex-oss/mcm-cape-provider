package net.litetex.capes.menu.provider.preview;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.util.Mth;


public class PlayerLimbAnimator
{
	private static final float LIMB_DISTANCE = -0.1f;
	private final int msBetweenUpdates;
	private long nextUpdateTimeMs;
	
	private float limbAngle;
	
	public PlayerLimbAnimator(final int fps)
	{
		this.msBetweenUpdates = 1000 / fps;
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	public void animate(final PlayerModel player, final float tickDelta)
	{
		if(player == null)
		{
			return;
		}
		
		final long currentTimeMs = System.currentTimeMillis();
		if(currentTimeMs > this.nextUpdateTimeMs)
		{
			this.nextUpdateTimeMs = currentTimeMs + this.msBetweenUpdates;
			
			this.limbAngle += LIMB_DISTANCE;
		}
		
		final float calcLimbAngle = this.limbAngle - LIMB_DISTANCE * (1.0f - tickDelta);
		
		final float a = calcLimbAngle * 0.6662f;
		player.rightArm.xRot = Mth.cos(a + 3.1415927f) * 2.0f * LIMB_DISTANCE * 0.5f;
		player.leftArm.xRot = Mth.cos(a) * 2.0f * LIMB_DISTANCE * 0.5f;
		player.rightLeg.xRot = Mth.cos(a) * 1.4f * LIMB_DISTANCE;
		player.leftLeg.xRot = Mth.cos(a + 3.1415927f) * 1.4f * LIMB_DISTANCE;
	}
}
