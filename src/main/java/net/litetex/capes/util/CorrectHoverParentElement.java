package net.litetex.capes.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;


public interface CorrectHoverParentElement extends ContainerEventHandler
{
	// Fixes click and hover not working when element/button is rendered on top!
	@Override
	default Optional<GuiEventListener> getChildAt(final double mouseX, final double mouseY)
	{
		// ParentElement#hoveredElement checks the elements in the incorrect order! (Bottom -> Top)
		// It should do that from top to bottom, as only the top-most element can be clicked/hovered!
		
		// Defensive copy against modification on the fly
		final List<? extends GuiEventListener> children = new ArrayList<>(this.children());
		for(int i = children.size() - 1; i >= 0; i--)
		{
			final GuiEventListener element = children.get(i);
			if(element.isMouseOver(mouseX, mouseY))
			{
				return Optional.of(element);
			}
		}
		
		return Optional.empty();
	}
}
