package net.litetex.capes.menu.provider.preview.render;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;


public record PlayerDisplayGuiElementRenderState(
	PlayerDisplayGuiModels models,
	PlayerDisplayGuiPayload payload,
	float rotationX,
	float rotationY,
	float pivotY,
	int x0,
	int y0,
	int x1,
	int y1,
	float scale,
	@Nullable ScreenRectangle scissorArea,
	@Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState
{
	@SuppressWarnings("PMD.ExcessiveParameterList") // Derived from MC code
	public PlayerDisplayGuiElementRenderState(
		final PlayerDisplayGuiModels models,
		final PlayerDisplayGuiPayload payload,
		final float rotationX,
		final float rotationY,
		final float pivotY,
		final int x0,
		final int y0,
		final int x1,
		final int y1,
		final float scale,
		@Nullable final ScreenRectangle screenRect
	)
	{
		this(
			models,
			payload,
			rotationX,
			rotationY,
			pivotY,
			x0,
			y0,
			x1,
			y1,
			scale,
			screenRect,
			PictureInPictureRenderState.getBounds(x0, y0, x1, y1, screenRect));
	}
}
