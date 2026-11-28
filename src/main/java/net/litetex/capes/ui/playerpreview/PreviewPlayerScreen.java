package net.litetex.capes.ui.playerpreview;

import java.util.ArrayList;
import java.util.List;

import net.litetex.capes.ui.playerpreview.render.PlayerDisplayWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;


public abstract class PreviewPlayerScreen<V extends ViewModel> extends Screen
{
	protected static final int DEFAULT_SPACING = 4;
	
	protected final Screen lastScreen;
	protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	
	// NOTE: This screen uses "self-manged" drawable children
	// aka widgets that are not directly attached to a layout and therefore "free floating" inside the screen
	//
	// 2026-04: Tried to use Layouts (LinearLayouts) but this didn't work and had a bunch of problems like:
	// * Scrollbars being not correctly respected
	// * Requiring double resizing to emulate a flexbox-like system
	// * ...
	// -> Use the manual approach
	private final List<GuiEventListener> selfManagedDrawableChilds = new ArrayList<>();
	
	protected final PlayerDisplayWidget playerWidget;
	
	protected final V viewModel;
	
	@SuppressWarnings("checkstyle:MagicNumber")
	protected PreviewPlayerScreen(
		final Minecraft client,
		final Screen lastScreen,
		final String title,
		final V viewModel)
	{
		super(client, client.font, Component.literal(title));
		this.lastScreen = lastScreen;
		this.viewModel = viewModel;
		
		this.initiallyAddHeader(this.layout);
		
		final PlayerAnimator playerAnimator = new PlayerAnimator(60);
		this.playerWidget = new PlayerDisplayWidget(
			120,
			120,
			client.getEntityModels(),
			viewModel::getPayload,
			playerAnimator::animate
		);
		this.playerWidget.rotationY = 185; // Default view = from behind, facing the cape/elytra
		
		this.initiallyAddFooter(this.layout);
	}
	
	protected void initiallyAddHeader(final HeaderAndFooterLayout layout)
	{
		layout.addTitleHeader(this.title, this.font);
	}
	
	protected void initiallyAddFooter(final HeaderAndFooterLayout layout)
	{
		layout.addToFooter(Button.builder(
				CommonComponents.GUI_DONE,
				_ -> this.onClose())
			.width(200)
			.build());
	}
	
	@Override
	protected void init()
	{
		this.layout.visitWidgets(this::addRenderableWidget);
		this.repositionElements();
	}
	
	protected abstract int initSelfManagedDrawableChildsDeterminePreviewX(int contentY, int contentWidth);
	
	@SuppressWarnings("checkstyle:MagicNumber")
	protected void initSelfMangedDrawableChilds()
	{
		final int contentY = this.layout.getHeaderHeight();
		final int contentWidth = Math.min(320, this.width - 88);
		
		final int previewX = this.initSelfManagedDrawableChildsDeterminePreviewX(contentY, contentWidth);
		final int rightAreaWidth = Math.max(this.width - previewX - DEFAULT_SPACING, 12);
		
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
			previewX
				// Center vertically
				+ Math.max((rightAreaWidth - this.playerWidget.getWidth()) / 2, 0),
			contentY);
		
		this.addSelfManagedDrawableChild(this.playerWidget);
		
		final Button btnToggleElytra = this.addSelfManagedDrawableChild(Button.builder(
				Component.literal("Toggle Elytra"),
				_ -> this.viewModel.toggleShowElytra())
			.pos(previewX, this.playerWidget.getBottom() + 2)
			.size(rightAreaWidth, 18)
			.build());
		this.addSelfManagedDrawableChild(Button.builder(
				Component.literal("Toggle Player"),
				_ -> this.viewModel.toggleShowBody())
			.pos(previewX, btnToggleElytra.getBottom() + DEFAULT_SPACING)
			.size(rightAreaWidth, 18)
			.build());
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
		
		this.layout.arrangeElements();
		
		this.initSelfMangedDrawableChilds();
	}
	// endregion
	
	@Override
	public void onClose()
	{
		this.minecraft.gui.setScreen(this.lastScreen);
	}
}
