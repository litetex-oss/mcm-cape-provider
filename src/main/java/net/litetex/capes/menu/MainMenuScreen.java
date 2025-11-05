package net.litetex.capes.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.litetex.capes.Capes;
import net.litetex.capes.config.Config;
import net.litetex.capes.i18n.CapesI18NKeys;
import net.litetex.capes.menu.other.OtherMenuScreen;
import net.litetex.capes.menu.preview.PreviewMenuScreen;
import net.litetex.capes.menu.provider.ProviderMenuScreen;
import net.litetex.capes.util.CorrectHoverParentElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;


public abstract class MainMenuScreen extends OptionsSubScreen implements CorrectHoverParentElement
{
	private final List<GuiEventListener> selfManagedDrawableChilds = new ArrayList<>();
	
	protected MainMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions, Component.translatable(CapesI18NKeys.CAPE_OPTIONS));
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	@Override
	protected void addContents()
	{
		// The first "row" is used by the buttons for the individual screens
		this.list = this.layout.addToContents(
			new HeaderHeightOptionListWidget(this.minecraft, this.width, this, 24));
		this.addOptions();
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	protected void initSelfMangedDrawableChilds()
	{
		final int buttonW = 100;
		final int offset = (buttonW / 2) + 5;
		
		record ButtonBuildData(
			String translationKey,
			Supplier<Screen> screenSupplier,
			int positionDiff,
			Class<?> clazz
		)
		{
		}
		
		Stream.of(
				new ButtonBuildData(
					CapesI18NKeys.PREVIEW,
					() -> new PreviewMenuScreen(this.lastScreen, this.options),
					-(buttonW / 2),
					PreviewMenuScreen.class
				),
				new ButtonBuildData(
					CapesI18NKeys.MANAGE_PROVIDERS,
					() -> new ProviderMenuScreen(this.lastScreen, this.options),
					-(buttonW + offset),
					ProviderMenuScreen.class),
				new ButtonBuildData(
					CapesI18NKeys.OTHER,
					() -> new OtherMenuScreen(this.lastScreen, this.options),
					offset,
					OtherMenuScreen.class
				))
			.forEach(data -> {
				final Button buttonWidget = this.addSelfManagedDrawableChild(Button.builder(
						Component.translatable(data.translationKey()),
						b -> this.minecraft.setScreen(data.screenSupplier().get()))
					.pos((this.width / 2) + data.positionDiff(), 35)
					.size(buttonW, 20)
					.build());
				buttonWidget.active = !(data.clazz().isInstance(this));
			});
	}
	
	protected <T extends GuiEventListener & Renderable & NarratableEntry> T addSelfManagedDrawableChild(final T drawableElement)
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
	
	@Override
	protected void addOptions()
	{
		// Nothing
	}
	
	protected Capes capes()
	{
		return Capes.instance();
	}
	
	protected Config config()
	{
		return this.capes().config();
	}
	
	@Override
	public void onClose()
	{
		super.onClose();
		this.capes().refreshIfMarked();
	}
	
	static class HeaderHeightOptionListWidget extends OptionsList
	{
		private final int headerHeight;
		
		public HeaderHeightOptionListWidget(
			final Minecraft client,
			final int width,
			final OptionsSubScreen optionsScreen,
			final int headerHeight)
		{
			super(client, width, optionsScreen);
			this.headerHeight = headerHeight;
		}
		
		@Override
		protected int getFirstEntryY()
		{
			return super.getFirstEntryY() + this.headerHeight;
		}
		
		@Override
		protected int contentHeight()
		{
			return super.contentHeight() + this.headerHeight;
		}
	}
}
