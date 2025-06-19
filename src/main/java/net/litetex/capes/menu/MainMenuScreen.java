package net.litetex.capes.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import net.litetex.capes.Capes;
import net.litetex.capes.CapesI18NKeys;
import net.litetex.capes.config.Config;
import net.litetex.capes.menu.other.OtherMenuScreen;
import net.litetex.capes.menu.preview.PreviewMenuScreen;
import net.litetex.capes.menu.provider.ProviderMenuScreen;
import net.litetex.capes.util.CorrectHoverParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;


public abstract class MainMenuScreen extends GameOptionsScreen implements CorrectHoverParentElement
{
	private final List<Element> selfManagedDrawableChilds = new ArrayList<>();
	
	protected MainMenuScreen(
		final Screen parent,
		final GameOptions gameOptions)
	{
		super(parent, gameOptions, Text.translatable(CapesI18NKeys.CAPE_OPTIONS));
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	@Override
	protected void initBody()
	{
		super.initBody();
		this.body.headerHeight = 28; // The first "row" is used by the buttons for the individual screens
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
					() -> new PreviewMenuScreen(this.parent, this.gameOptions),
					-(buttonW / 2),
					PreviewMenuScreen.class
				),
				new ButtonBuildData(
					CapesI18NKeys.MANAGE_PROVIDERS,
					() -> new ProviderMenuScreen(this.parent, this.gameOptions),
					-(buttonW + offset),
					ProviderMenuScreen.class),
				new ButtonBuildData(
					CapesI18NKeys.OTHER,
					() -> new OtherMenuScreen(this.parent, this.gameOptions),
					offset,
					OtherMenuScreen.class
				))
			.forEach(data -> {
				final ButtonWidget buttonWidget = this.addSelfManagedDrawableChild(ButtonWidget.builder(
						Text.translatable(data.translationKey()),
						b -> this.client.setScreen(data.screenSupplier().get()))
					.position((this.width / 2) + data.positionDiff(), 35)
					.size(buttonW, 20)
					.build());
				buttonWidget.active = !(data.clazz().isInstance(this));
			});
	}
	
	protected <T extends Element & Drawable & Selectable> T addSelfManagedDrawableChild(final T drawableElement)
	{
		this.selfManagedDrawableChilds.add(drawableElement);
		return this.addDrawableChild(drawableElement);
	}
	
	@Override
	protected void clearChildren()
	{
		this.selfManagedDrawableChilds.clear();
		super.clearChildren();
	}
	
	@Override
	protected void refreshWidgetPositions()
	{
		this.selfManagedDrawableChilds.forEach(this::remove);
		this.selfManagedDrawableChilds.clear();
		
		super.refreshWidgetPositions();
		
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
	public void close()
	{
		super.close();
		this.capes().refreshIfMarked();
	}
}
