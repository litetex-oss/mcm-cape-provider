package net.litetex.capes.provider.builtin.sneakycapes.pixels;

import java.util.List;

import com.mojang.blaze3d.platform.NativeImage;


public final class SneakyCapesCopyPixels
{
	static final List<CopyRectCoordinates> COPY_RECTS = List.of(
		// copy cape from skin
		// front
		new CopyRectCoordinates(56, 16, 1, 1, 8, 16),
		new CopyRectCoordinates(62, 0, 9, 1, 2, 8),
		new CopyRectCoordinates(60, 0, 9, 9, 2, 8),
		// back
		new CopyRectCoordinates(56, 32, 12, 1, 8, 16),
		new CopyRectCoordinates(58, 0, 20, 1, 2, 8),
		new CopyRectCoordinates(56, 0, 20, 9, 2, 8),
		// left edge
		new CopyRectCoordinates(39, 0, 0, 1, 1, 8),
		new CopyRectCoordinates(38, 0, 0, 9, 1, 8),
		// right edge
		new CopyRectCoordinates(37, 0, 11, 1, 1, 8),
		new CopyRectCoordinates(36, 0, 11, 9, 1, 8),
		// top edge
		new CopyRectCoordinates(0, 48, 1, 0, 4, 1),
		new CopyRectCoordinates(12, 48, 5, 0, 6, 1),
		// bottom edge
		new CopyRectCoordinates(0, 49, 11, 0, 4, 1),
		new CopyRectCoordinates(12, 49, 15, 0, 6, 1),
		// copy elytra from skin
		// front  (10x20)
		new CopyRectCoordinates(0, 0, 36, 2, 8, 8),
		new CopyRectCoordinates(24, 0, 44, 2, 2, 8),
		new CopyRectCoordinates(26, 0, 36, 10, 10, 8),
		new CopyRectCoordinates(44, 48, 36, 18, 8, 4),
		new CopyRectCoordinates(62, 48, 44, 18, 2, 4),
		// inside edge (2x12)
		new CopyRectCoordinates(18, 48, 34, 2, 2, 4),
		new CopyRectCoordinates(28, 48, 34, 6, 2, 4),
		new CopyRectCoordinates(30, 48, 34, 10, 2, 4),
		// outside edge (1x12)
		new CopyRectCoordinates(32, 48, 22, 10, 1, 4),
		new CopyRectCoordinates(33, 48, 22, 14, 1, 4),
		new CopyRectCoordinates(34, 48, 22, 18, 1, 4),
		// top edge (4x2)
		new CopyRectCoordinates(0, 50, 30, 0, 4, 2),
		// bottom edge (6x2)
		new CopyRectCoordinates(12, 50, 34, 0, 6, 2)
	);
	
	public static void bodyToCape(final NativeImage body, final NativeImage cape)
	{
		COPY_RECTS.forEach(r ->
			body.copyRect(cape, r.bodyX(), r.bodyY(), r.capeX(), r.capeY(), r.sizeX(), r.sizeY(), false, false));
	}
	
	public static void capeToBody(final NativeImage cape, final NativeImage body)
	{
		COPY_RECTS.forEach(r ->
			cape.copyRect(body, r.capeX(), r.capeY(), r.bodyX(), r.bodyY(), r.sizeX(), r.sizeY(), false, false));
	}
	
	record CopyRectCoordinates(
		int bodyX,
		int bodyY,
		int capeX,
		int capeY,
		int sizeX,
		int sizeY
	)
	{
	}
	
	private SneakyCapesCopyPixels()
	{
	}
}
