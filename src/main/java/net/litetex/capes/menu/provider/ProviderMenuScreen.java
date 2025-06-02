package net.litetex.capes.menu.provider;

import net.litetex.capes.menu.MainMenuScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;


public class ProviderMenuScreen extends MainMenuScreen
{
	public ProviderMenuScreen(
		final Screen parent,
		final GameOptions gameOptions)
	{
		super(parent, gameOptions);
	}
	
	@Override
	protected void initSelfMangedDrawableChilds()
	{
		super.initSelfMangedDrawableChilds();
		
		final int offset = 28;
		final ProviderListWidget providerListWidget = this.addSelfManagedDrawableChild(new ProviderListWidget(
			MinecraftClient.getInstance(),
			this.body.getRowWidth(),
			this.body.getHeight() - offset - 4,
			this));
		providerListWidget.setPosition(
			this.body.getX() + (this.body.getWidth() - this.body.getRowWidth()) / 2,
			this.body.getY() + offset);
	}
}
