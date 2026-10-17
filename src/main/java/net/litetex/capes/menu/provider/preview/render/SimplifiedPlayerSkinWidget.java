package net.litetex.capes.menu.provider.preview.render;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;


// See PlayerSkinWidget
@SuppressWarnings({"checkstyle:VisibilityModifier", "checkstyle:MagicNumber"})
public abstract class SimplifiedPlayerSkinWidget extends AbstractWidget
{
	public float rotationX = -5.0F;
	public float rotationY = 30.0F;
	
	public SimplifiedPlayerSkinWidget(
		final int width,
		final int height)
	{
		super(0, 0, width, height, CommonComponents.EMPTY);
	}
	
	@Override
	protected void onDrag(final MouseButtonEvent event, final double dx, final double dy)
	{
		this.rotationX = Mth.clamp(this.rotationX - (float)dy * 2.5F, -50.0F, 50.0F);
		this.rotationY += (float)dx * 2.5F;
	}
	
	@Override
	public void playDownSound(final SoundManager soundManager)
	{
	}
	
	@Override
	protected void updateWidgetNarration(final NarrationElementOutput output)
	{
	}
	
	@Nullable
	@Override
	public ComponentPath nextFocusPath(final FocusNavigationEvent navigationEvent)
	{
		return null;
	}
}
