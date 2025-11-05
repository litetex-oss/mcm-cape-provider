package net.litetex.capes.menu.other;

import java.util.List;

import net.litetex.capes.Capes;
import net.litetex.capes.config.AnimatedCapesHandling;
import net.litetex.capes.config.ModProviderHandling;
import net.litetex.capes.i18n.CapesI18NKeys;
import net.litetex.capes.menu.MainMenuScreen;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;


public class OtherMenuScreen extends MainMenuScreen
{
	public OtherMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions);
	}
	
	@Override
	protected void addOptions()
	{
		final Capes capes = Capes.instance();
		
		this.list.addSmall(List.of(
			CycleButton.onOffBuilder(capes.config().isOnlyLoadForSelf())
				.create(
					Component.translatable(CapesI18NKeys.ONLY_LOAD_YOUR_CAPE),
					(btn, enabled) -> {
						this.config().setOnlyLoadForSelf(enabled);
						capes.saveConfigAndMarkRefresh();
					}),
			CycleButton.<AnimatedCapesHandling>builder(handling ->
					switch(handling)
					{
						case ON -> CommonComponents.OPTION_ON;
						case FROZEN -> Component.translatable(CapesI18NKeys.FROZEN);
						case OFF -> CommonComponents.OPTION_OFF;
					})
				.withInitialValue(this.config().getAnimatedCapesHandling())
				.withValues(AnimatedCapesHandling.values())
				.create(
					Component.translatable(CapesI18NKeys.ANIMATED_TEXTURES),
					(btn, value) -> {
						this.config().setAnimatedCapesHandling(value);
						capes.saveConfigAndMarkRefresh();
					}),
			CycleButton.onOffBuilder(capes.config().isEnableElytraTexture())
				.create(
					Component.translatable(CapesI18NKeys.ELYTRA_TEXTURE),
					(btn, enabled) -> {
						this.config().setEnableElytraTexture(enabled);
						capes.saveConfig();
					}),
			CycleButton.<ModProviderHandling>builder(handling ->
					switch(handling)
					{
						case ON -> CommonComponents.OPTION_ON;
						case ONLY_LOAD -> Component.translatable(CapesI18NKeys.LOAD);
						case OFF -> CommonComponents.OPTION_OFF;
					})
				.withInitialValue(this.config().getModProviderHandling())
				.withValues(ModProviderHandling.values())
				.withTooltip(value -> Tooltip.create(
					Component.translatable(CapesI18NKeys.LOAD_PROVIDERS)
						.append(": ")
						.append(CommonComponents.optionStatus(value.load()))
						.append("\n")
						.append(Component.translatable(CapesI18NKeys.ACTIVATE_PROVIDERS_BY_DEFAULT))
						.append(": ")
						.append(CommonComponents.optionStatus(value.activateByDefault()))))
				.create(
					Component.translatable(CapesI18NKeys.PROVIDERS_FROM_MODS),
					(btn, value) -> {
						this.config().setModProviderHandling(value);
						capes.saveConfig();
					})
		));
		
		this.list.addSmall(
			Button.builder(
				Component.translatable("controls.reset"), btn -> {
					this.config().reset();
					capes.saveConfigAndMarkRefresh();
					
					// Recreate screen
					this.minecraft.setScreen(new OtherMenuScreen(this.lastScreen, this.options));
				}).build(),
			null);
	}
}
