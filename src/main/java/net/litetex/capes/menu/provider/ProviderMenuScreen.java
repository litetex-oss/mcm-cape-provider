package net.litetex.capes.menu.provider;

import net.litetex.capes.menu.MainMenuScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;


public class ProviderMenuScreen extends MainMenuScreen
{
	public ProviderMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions);
	}
	
	@Override
	protected void initSelfMangedDrawableChilds()
	{
		super.initSelfMangedDrawableChilds();
		
		final int offset = 28;
		final ProviderListWidget providerListWidget = this.addSelfManagedDrawableChild(new ProviderListWidget(
			Minecraft.getInstance(),
			this.list.getRowWidth(),
			this.list.getHeight() - offset - 4,
			this));
		providerListWidget.setPosition(
			this.list.getX() + (this.list.getWidth() - this.list.getRowWidth()) / 2,
			this.list.getY() + offset);
	}
}
