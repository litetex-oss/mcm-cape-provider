package net.litetex.capes.provider.builtin.sneakycapes.pixels;

import java.util.List;

import com.mojang.blaze3d.platform.NativeImage;


public final class SneakyCapesMarkerPixels
{
	static final List<MarkerPixelInfo> MARKER_PIXELS = List.of(
		new MarkerPixelInfo(60, 48, 0xFFFFF42F),
		new MarkerPixelInfo(60, 49, 0xFFFFFFFF),
		new MarkerPixelInfo(60, 50, 0xFF9C59D1),
		new MarkerPixelInfo(60, 51, 0xFF292929)
	);
	
	public static boolean markersPresent(final NativeImage body)
	{
		return MARKER_PIXELS.stream()
			.allMatch(marker -> body.getPixel(marker.x(), marker.y()) == marker.rgba());
	}
	
	public static void applyMarkers(final NativeImage body)
	{
		MARKER_PIXELS.forEach(marker -> body.setPixel(marker.x(), marker.y(), marker.rgba()));
	}
	
	record MarkerPixelInfo(
		int x,
		int y,
		int rgba
	)
	{
	}
	
	private SneakyCapesMarkerPixels()
	{
	}
}
