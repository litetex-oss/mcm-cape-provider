package net.litetex.capes.menu.preview.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;


public record PlayerDisplayGuiElementRenderState(
	PlayerDisplayGuiModels models,
	PlayerDisplayGuiPayload payload,
	float xRotation,
	float yRotation,
	float yPivot,
	int x1,
	int y1,
	int x2,
	int y2,
	float scale,
	@Nullable ScreenRect scissorArea,
	@Nullable ScreenRect bounds
) implements SpecialGuiElementRenderState
{
	@SuppressWarnings("PMD.ExcessiveParameterList") // Derived from MC code
	public PlayerDisplayGuiElementRenderState(
		final PlayerDisplayGuiModels models,
		final PlayerDisplayGuiPayload payload,
		final float xRotation,
		final float yRotation,
		final float yPivot,
		final int x1,
		final int y1,
		final int x2,
		final int y2,
		final float scale,
		@Nullable final ScreenRect screenRect
	)
	{
		this(
			models,
			payload,
			xRotation,
			yRotation,
			yPivot,
			x1,
			y1,
			x2,
			y2,
			scale,
			screenRect,
			SpecialGuiElementRenderState.createBounds(x1, y1, x2, y2, screenRect));
	}
}
