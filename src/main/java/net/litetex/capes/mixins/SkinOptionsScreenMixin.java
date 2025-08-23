package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.litetex.capes.Capes;
import net.litetex.capes.menu.preview.PreviewMenuScreen;
import net.litetex.capes.util.CorrectHoverParentElement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SkinOptionsScreen;
import net.minecraft.client.gui.widget.TextIconButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


@Mixin(SkinOptionsScreen.class)
public abstract class SkinOptionsScreenMixin extends GameOptionsScreen implements CorrectHoverParentElement
{
	@Unique
	private static final Identifier CAPE_OPTIONS_ICON_TEXTURE = Identifier.of(Capes.MOD_ID, "icon/cape_options");
	
	@Unique
	private TextIconButtonWidget btnCapeMenu;
	
	protected SkinOptionsScreenMixin(
		final Screen parent,
		final GameOptions gameOptions,
		final Text title)
	{
		super(parent, gameOptions, title);
	}
	
	@SuppressWarnings("checkstyle:MagicNumber")
	@Override
	protected void init()
	{
		super.init();
		
		// It's important that this is added after all other elements have been added
		// Else it's rendered behind other elements
		this.btnCapeMenu = this.addDrawableChild(TextIconButtonWidget.builder(
				Text.empty(),
				ignored -> this.client.setScreen(new PreviewMenuScreen(
					this,
					this.gameOptions)),
				true)
			.dimension(20, 20)
			.texture(CAPE_OPTIONS_ICON_TEXTURE, 16, 16)
			.build());
		
		this.updateRelativePositions();
	}
	
	@Unique
	@SuppressWarnings("checkstyle:MagicNumber")
	private void updateRelativePositions()
	{
		this.btnCapeMenu.setPosition(this.body.getRowLeft() - 25, this.body.getY() + 4);
	}
	
	@Override
	protected void refreshWidgetPositions()
	{
		super.refreshWidgetPositions();
		
		if(this.btnCapeMenu != null) // Init check
		{
			this.updateRelativePositions();
		}
	}
}
