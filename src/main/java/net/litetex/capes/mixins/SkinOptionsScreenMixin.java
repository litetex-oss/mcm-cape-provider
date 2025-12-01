package net.litetex.capes.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.litetex.capes.Capes;
import net.litetex.capes.menu.preview.PreviewMenuScreen;
import net.litetex.capes.util.CorrectHoverParentElement;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.SkinCustomizationScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


@Mixin(SkinCustomizationScreen.class)
public abstract class SkinOptionsScreenMixin extends OptionsSubScreen implements CorrectHoverParentElement
{
	@Unique
	private static final ResourceLocation CAPE_OPTIONS_ICON_TEXTURE =
		ResourceLocation.fromNamespaceAndPath(Capes.MOD_ID, "icon/cape_options");
	
	@Unique
	private SpriteIconButton btnCapeMenu;
	
	protected SkinOptionsScreenMixin(
		final Screen parent,
		final Options gameOptions,
		final Component title)
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
		this.btnCapeMenu = this.addRenderableWidget(SpriteIconButton.builder(
				Component.empty(),
				ignored -> this.minecraft.setScreen(new PreviewMenuScreen(
					this,
					this.options)),
				true)
			.size(20, 20)
			.sprite(CAPE_OPTIONS_ICON_TEXTURE, 16, 16)
			.build());
		
		this.updateRelativePositions();
	}
	
	@Unique
	@SuppressWarnings("checkstyle:MagicNumber")
	private void updateRelativePositions()
	{
		this.btnCapeMenu.setPosition(this.list.getRowLeft() - 25, this.list.getY() + 4);
	}
	
	@Override
	protected void repositionElements()
	{
		super.repositionElements();
		
		if(this.btnCapeMenu != null) // Init check
		{
			this.updateRelativePositions();
		}
	}
}
