package net.litetex.capes.menu.provider.preview;

import net.litetex.capes.menu.provider.preview.render.PlayerDisplayGuiModels;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;


public class PlayerAnimator
{
	private static final float LIMB_DISTANCE = -0.05f;
	
	private final int msBetweenUpdates;
	private long nextUpdateTimeMs;
	
	public PlayerAnimator(final int fps)
	{
		this.msBetweenUpdates = 1000 / fps;
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	public void animate(final PlayerDisplayGuiModels player)
	{
		if(player == null)
		{
			return;
		}
		
		final long currentTimeMs = System.currentTimeMillis();
		if(currentTimeMs < this.nextUpdateTimeMs)
		{
			return;
		}
		
		this.nextUpdateTimeMs = currentTimeMs + this.msBetweenUpdates;
		
		final AvatarRenderState state = player.state();
		
		// See also: WalkAnimationState
		state.walkAnimationPos += LIMB_DISTANCE;
	}
}
