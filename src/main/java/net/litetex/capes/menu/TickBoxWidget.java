package net.litetex.capes.menu;

import java.util.function.BiConsumer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;


@SuppressWarnings("checkstyle:MagicNumber")
public class TickBoxWidget extends ClickableWidget
{
	private boolean ticked;
	private final BiConsumer<TickBoxWidget, Boolean> onTickChanged;
	
	public TickBoxWidget(
		final int size,
		final boolean ticked,
		final boolean readOnly,
		final BiConsumer<TickBoxWidget, Boolean> onTickChanged)
	{
		super(0, 0, size, size, Text.empty());
		this.active = !readOnly;
		this.ticked = ticked;
		this.onTickChanged = onTickChanged;
	}
	
	@Override
	protected void renderWidget(final DrawContext context, final int mouseX, final int mouseY, final float delta)
	{
		final int x = this.getX();
		final int y = this.getY();
		final int xEnd = x + this.getWidth();
		final int yEnd = y + this.getHeight();
		
		final int color = this.active ? 0xFFFFFFFF : 0xFFAAAAAA;
		
		if(this.ticked)
		{
			context.fill(x + 2, y + 2, xEnd - 2, yEnd - 2, color);
		}
		
		this.drawBorder(context, x, y, xEnd, yEnd, color);
	}
	
	private void drawBorder(
		final DrawContext context,
		final int x1,
		final int y1,
		final int x2,
		final int y2,
		final int color)
	{
		context.fill(x1, y1, x2, y1 + 1, color);
		context.fill(x1, y2 - 1, x2, y2, color);
		context.fill(x1, y1, x1 + 1, y2, color);
		context.fill(x2 - 1, y1, x2, y2, color);
	}
	
	@Override
	public void onClick(final double mouseX, final double mouseY)
	{
		this.toggle();
	}
	
	public void toggle()
	{
		if(this.active)
		{
			this.ticked = !this.ticked;
			this.onTickChanged.accept(this, this.ticked);
		}
	}
	
	public boolean isTicked()
	{
		return this.ticked;
	}
	
	@Override
	protected void appendClickableNarrations(final NarrationMessageBuilder builder)
	{
	}
}
