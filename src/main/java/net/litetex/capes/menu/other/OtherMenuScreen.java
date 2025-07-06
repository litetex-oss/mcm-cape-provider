package net.litetex.capes.menu.other;

import java.util.List;

import net.litetex.capes.Capes;
import net.litetex.capes.config.AnimatedCapesHandling;
import net.litetex.capes.i18n.CapesI18NKeys;
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
			CyclingButtonWidget.onOffBuilder(capes.config().isOnlyLoadForSelf())
				.build(
					Text.translatable(CapesI18NKeys.ONLY_LOAD_YOUR_CAPE),
					(btn, enabled) -> {
						this.config().setOnlyLoadForSelf(enabled);
						capes.saveConfigAndMarkRefresh();
					}),
			CyclingButtonWidget.<AnimatedCapesHandling>builder(handling ->
					switch(handling)
					{
						case ON -> ScreenTexts.ON;
						case FROZEN -> Text.translatable(CapesI18NKeys.FROZEN);
						case OFF -> ScreenTexts.OFF;
					})
				.initially(this.config().getAnimatedCapesHandling())
				.values(AnimatedCapesHandling.values())
				.build(
					Text.translatable(CapesI18NKeys.ANIMATED_TEXTURES),
					(btn, value) -> {
						this.config().setAnimatedCapesHandling(value);
						capes.saveConfigAndMarkRefresh();
					}),
			CyclingButtonWidget.onOffBuilder(capes.config().isEnableElytraTexture())
				.build(
					Text.translatable(CapesI18NKeys.ELYTRA_TEXTURE),
					(btn, enabled) -> {
						this.config().setEnableElytraTexture(enabled);
						capes.saveConfig();
					})
		));
		
		this.body.addWidgetEntry(
			ButtonWidget.builder(
				Text.translatable("controls.reset"), btn -> {
					this.config().reset();
					capes.saveConfigAndMarkRefresh();
					
					// Recreate screen
					this.client.setScreen(new OtherMenuScreen(this.parent, this.gameOptions));
				}).build(),
			null);
	}
}
