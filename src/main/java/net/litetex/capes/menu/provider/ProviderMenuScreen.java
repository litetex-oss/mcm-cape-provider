package net.litetex.capes.menu.provider;

import net.litetex.capes.Capes;
import net.litetex.capes.menu.advanced.AdvancedMenuScreen;
import net.litetex.capes.ui.playerpreview.PreviewPlayerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;


@SuppressWarnings("checkstyle:MagicNumber")
public class ProviderMenuScreen extends PreviewPlayerScreen<ViewModel>
{
	public static final String CAPE_OPTIONS = "Cape Options";
	
	public ProviderMenuScreen(final Minecraft client, final Screen lastScreen)
	{
		super(client, lastScreen, CAPE_OPTIONS, new ViewModel());
	}
	
	@Override
	protected void initiallyAddFooter(final HeaderAndFooterLayout layout)
	{
		final LinearLayout footerLayout = layout.addToFooter(LinearLayout.horizontal().spacing(8));
		footerLayout.addChild(Button.builder(
				Component.literal("Advanced"),
				_ -> this.minecraft.setScreenAndShow(new AdvancedMenuScreen(this, this.minecraft.options)))
			.build());
		footerLayout.addChild(Button.builder(
				CommonComponents.GUI_DONE,
				_ -> this.onClose())
			.build());
	}
	
	@Override
	protected int initSelfManagedDrawableChildsDeterminePreviewX(final int contentY, final int contentWidth)
	{
		final ProviderListWidget providerListWidget = this.addSelfManagedDrawableChild(new ProviderListWidget(
			this.minecraft,
			contentWidth,
			this.layout.getContentHeight(),
			this,
			this.viewModel::changeProviderTo
		));
		providerListWidget.setPosition(DEFAULT_SPACING, contentY);
		
		return providerListWidget.getRight()
			+ DEFAULT_SPACING
			// Scrollbar does not count to providerListWidget's width
			+ (providerListWidget.maxScrollAmount() > 0 ? 12 : 0);
	}
	
	@Override
	public void onClose()
	{
		super.onClose();
		Capes.instance().refreshIfMarked();
	}
}
