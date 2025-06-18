package net.litetex.capes.menu.other;

import java.util.List;

import net.litetex.capes.Capes;
import net.litetex.capes.CapesI18NKeys;
import net.litetex.capes.config.AnimatedTexturesHandling;
import net.litetex.capes.menu.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;


public class OtherMenuScreen extends MainMenuScreen
{
	public OtherMenuScreen(
		final Screen parent,
		final GameOptions gameOptions)
	{
		super(parent, gameOptions);
	}
	
	@Override
	protected void addOptions()
	{
		final Capes capes = Capes.instance();
		
		this.body.addAll(List.of(
			CyclingButtonWidget.<AnimatedTexturesHandling>builder(handling ->
					switch(handling)
					{
						case ON -> ScreenTexts.ON;
						case FROZEN -> Text.translatable(CapesI18NKeys.FROZEN);
						case OFF -> ScreenTexts.OFF;
					})
				.initially(capes.config().getAnimatedTexturesHandling())
				.values(AnimatedTexturesHandling.values())
				.build(
					Text.translatable(CapesI18NKeys.ANIMATED_TEXTURES),
					(btn, value) -> {
						capes.config().setAnimatedTexturesHandling(value);
						capes.saveConfig();
					}),
			CyclingButtonWidget.onOffBuilder(capes.config().isEnableElytraTexture())
				.build(
					Text.translatable(CapesI18NKeys.ELYTRA_TEXTURE),
					(btn, enabled) -> {
						capes.config().setEnableElytraTexture(enabled);
						capes.saveConfig();
					})
		));
		
		this.body.addWidgetEntry(
			ButtonWidget.builder(
				Text.translatable("controls.reset"), btn -> {
					capes.config().reset();
					capes.saveConfig();
					
					// Recreate screen
					this.client.setScreen(new OtherMenuScreen(this.parent, this.gameOptions));
				}).build(),
			null);
	}
}
