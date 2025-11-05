package net.litetex.capes.menu.preview.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;


public record PlayerDisplayGuiElementRenderState(
	PlayerDisplayGuiModels models,
	PlayerDisplayGuiPayload payload,
	float xRotation,
	float yRotation,
	float yPivot,
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
		final float xRotation,
		final float yRotation,
		final float yPivot,
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
			xRotation,
			yRotation,
			yPivot,
			x0,
			y0,
			x1,
			y1,
			scale,
			screenRect,
			PictureInPictureRenderState.getBounds(x0, y0, x1, y1, screenRect));
	}
}
