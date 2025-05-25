package net.litetex.capes.menu.preview.render;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;


public record PlayerDisplayGuiElementRenderState(
	PlayerEntityModel playerModel,
	Identifier texture,
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
	public PlayerDisplayGuiElementRenderState(
		final PlayerEntityModel playerEntityModel,
		final Identifier identifier,
		final float f,
		final float g,
		final float h,
		final int i,
		final int j,
		final int k,
		final int l,
		final float m,
		@Nullable final ScreenRect screenRect
	)
	{
		this(
			playerEntityModel,
			identifier,
			f,
			g,
			h,
			i,
			j,
			k,
			l,
			m,
			screenRect,
			SpecialGuiElementRenderState.createBounds(i, j, k, l, screenRect));
	}
}
