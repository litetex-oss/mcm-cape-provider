package net.litetex.capes.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;


public interface CorrectHoverParentElement extends ParentElement
{
	// Fixes click and hover not working when element our button is rendered on top!
	@Override
	default Optional<Element> hoveredElement(final double mouseX, final double mouseY)
	{
		// ParentElement#hoveredElement checks the elements in the incorrect order! (Bottom -> Top)
		// It should do that from top to bottom, as only the top-most element can be clicked/hovered!
		
		// Defensive copy against modification on the fly
		final List<? extends Element> children = new ArrayList<>(this.children());
		for(int i = children.size() - 1; i >= 0; i--)
		{
			final Element element = children.get(i);
			if(element.isMouseOver(mouseX, mouseY))
			{
				return Optional.of(element);
			}
		}
		
		return Optional.empty();
	}
}
