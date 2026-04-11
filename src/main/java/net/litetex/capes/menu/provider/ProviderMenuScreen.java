package net.litetex.capes.menu.provider;

import java.util.ArrayList;
import java.util.List;

import net.litetex.capes.Capes;
import net.litetex.capes.menu.advanced.AdvancedMenuScreen;
import net.litetex.capes.menu.provider.preview.PlayerLimbAnimator;
import net.litetex.capes.menu.provider.preview.ViewModel;
import net.litetex.capes.menu.provider.preview.render.PlayerDisplayWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;


@SuppressWarnings("checkstyle:MagicNumber")
public class ProviderMenuScreen extends OptionsSubScreen
{
	private static final int DEFAULT_SPACING = 4;
	
	// NOTE: This screen uses "self-manged" drawable children
	// aka widgets that are not directly attached to a layout and therefore "free floating" inside the screen
	//
	// 2026-04: Tried to use Layouts (LinearLayouts) but this didn't work and had a bunch of problems like:
	// * Scrollbars being not correctly respected
	// * Requiring double resizing to emulate a flexbox-like system
	// * ...
	// -> Use the manual approach
	private final List<GuiEventListener> selfManagedDrawableChilds = new ArrayList<>();
	
	private final PlayerDisplayWidget playerWidget;
	private final ViewModel viewModel = new ViewModel();
	
	public ProviderMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions, Component.literal("Cape Options"));
		
		final PlayerLimbAnimator playerLimbAnimator = new PlayerLimbAnimator(60);
		this.playerWidget = new PlayerDisplayWidget(
			120,
			120,
			Minecraft.getInstance().getEntityModels(),
			this.viewModel::getPayload,
			models -> playerLimbAnimator.animate(models.player(), 1));
		this.playerWidget.rotationY = 185; // Default view = from behind, facing the cape/elytra
	}
	
	protected void initSelfMangedDrawableChilds()
	{
		final ProviderListWidget providerListWidget = this.addSelfManagedDrawableChild(new ProviderListWidget(
			Minecraft.getInstance(),
			Math.min(320, this.width - 88),
			this.layout.getContentHeight(),
			this,
			this.viewModel::changeProviderTo
		));
		final int contentY = this.layout.getHeaderHeight();
		providerListWidget.setPosition(4, contentY);
		
		final int providerListContainerWidth = providerListWidget.getRight()
			+ DEFAULT_SPACING
			// Scrollbar does not count to providerListWidget's width
			+ (providerListWidget.maxScrollAmount() > 0 ? 12 : 0);
		final int rightAreaWidth = Math.max(this.width - providerListContainerWidth - DEFAULT_SPACING, 12);
		
		this.playerWidget.setWidth(rightAreaWidth);
		this.playerWidget.setHeight(Math.clamp(
			contentY + this.layout.getContentHeight()
				- (18 * 2) // 2x Buttons
				- DEFAULT_SPACING // Padding between buttons
				- 2 // Padding between button and player widget
				- contentY,
			32,
			rightAreaWidth * 2));
		
		this.playerWidget.setPosition(
			providerListContainerWidth
				// Center vertically
				+ Math.max((rightAreaWidth - this.playerWidget.getWidth()) / 2, 0),
			contentY);
		
		this.addSelfManagedDrawableChild(this.playerWidget);
		
		final Button btnToggleElytra = this.addSelfManagedDrawableChild(Button.builder(
				Component.literal("Toggle Elytra"),
				_ -> this.viewModel.toggleShowElytra())
			.pos(providerListContainerWidth, this.playerWidget.getBottom() + 2)
			.size(rightAreaWidth, 18)
			.build());
		this.addSelfManagedDrawableChild(Button.builder(
				Component.literal("Toggle Player"),
				_ -> this.viewModel.toggleShowBody())
			.pos(providerListContainerWidth, btnToggleElytra.getBottom() + DEFAULT_SPACING)
			.size(rightAreaWidth, 18)
			.build());
	}
	
	@Override
	protected void addFooter()
	{
		final LinearLayout footerLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
		
		footerLayout.addChild(Button.builder(
				Component.literal("Advanced"),
				_ -> this.minecraft.setScreen(new AdvancedMenuScreen(this, this.options)))
			.build());
		footerLayout.addChild(Button.builder(
				CommonComponents.GUI_DONE,
				_ -> this.onClose())
			.build());
	}
	
	@Override
	public void onClose()
	{
		super.onClose();
		Capes.instance().refreshIfMarked();
	}
	
	@Override
	protected void addContents()
	{
		// Nothing to do
	}
	
	@Override
	protected void addOptions()
	{
		// Nothing to do
	}
	
	// region Self-Managed Drawable Child
	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addSelfManagedDrawableChild(
		final T drawableElement)
	{
		this.selfManagedDrawableChilds.add(drawableElement);
		return this.addRenderableWidget(drawableElement);
	}
	
	@Override
	protected void clearWidgets()
	{
		this.selfManagedDrawableChilds.clear();
		super.clearWidgets();
	}
	
	@Override
	protected void repositionElements()
	{
		this.selfManagedDrawableChilds.forEach(this::removeWidget);
		this.selfManagedDrawableChilds.clear();
		
		super.repositionElements();
		
		this.initSelfMangedDrawableChilds();
	}
	// endregion
}
