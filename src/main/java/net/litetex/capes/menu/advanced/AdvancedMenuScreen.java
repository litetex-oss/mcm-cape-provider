package net.litetex.capes.menu.advanced;

import java.util.List;

import net.litetex.capes.Capes;
import net.litetex.capes.config.AnimatedCapesHandling;
import net.litetex.capes.config.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;


public class AdvancedMenuScreen extends OptionsSubScreen
{
	public AdvancedMenuScreen(
		final Screen parent,
		final Options gameOptions)
	{
		super(parent, gameOptions, Component.literal("Cape Options - Advanced"));
	}
	
	@Override
	protected void addOptions()
	{
		final Capes capes = Capes.instance();
		final Config config = capes.config();
		
		this.list.addSmall(List.of(
			CycleButton.onOffBuilder(config.isOnlyLoadForSelf())
				.create(
					Component.literal("Only load your cape"),
					(btn, enabled) -> {
						config.setOnlyLoadForSelf(enabled);
						capes.saveConfigAndMarkRefresh();
					}),
			CycleButton.<AnimatedCapesHandling>builder(handling ->
					switch(handling)
					{
						case ON -> CommonComponents.OPTION_ON;
						case FROZEN -> Component.literal("Frozen");
						case OFF -> CommonComponents.OPTION_OFF;
					},
					config::getAnimatedCapesHandling)
				.withValues(AnimatedCapesHandling.values())
				.create(
					Component.literal("Animated textures"),
					(_, value) -> {
						config.setAnimatedCapesHandling(value);
						capes.saveConfigAndMarkRefresh();
					}),
			CycleButton.onOffBuilder(config.isEnableElytraTexture())
				.create(
					Component.literal("Elytra texture"),
					(_, enabled) -> {
						config.setEnableElytraTexture(enabled);
						capes.saveConfig();
					}),
			CycleButton.onOffBuilder(config.isActivateExternalProvidersOnInitialLoad())
				.withTooltip(value -> Tooltip.create(Component.empty()
					.append(Component.literal("Should external providers (mods/local) be automatically "
						+ "activated/enabled when initially loaded?\n"))
					.append(this.requiresRestartComponent())))
				.create(
					Component.literal("Activate ext. providers"),
					(_, enabled) -> {
						config.setActivateExternalProvidersOnInitialLoad(enabled);
						capes.saveConfig();
					}),
			CycleButton.onOffBuilder(config.isLoadProvidersFromMods())
				.withTooltip(_ -> Tooltip.create(Component.empty()
					.append("Should providers be loaded from other mods?\n")
					.append(this.requiresRestartComponent())))
				.create(
					Component.literal("Load Mod providers"),
					(_, enabled) -> {
						config.setLoadProvidersFromMods(enabled);
						capes.saveConfig();
					}),
			CycleButton.onOffBuilder(config.isLoadSimpleLocalProvidersFromFilesystem())
				.withTooltip(_ -> Tooltip.create(Component.empty()
					.append("Should simple-local providers be loaded from "
						+ "config/cape-provider or config/cape-provider/simple-custom/...?\n")
					.append(this.requiresRestartComponent())))
				.create(
					Component.literal("Load local providers"),
					(_, enabled) -> {
						config.setLoadSimpleLocalProvidersFromFilesystem(enabled);
						capes.saveConfig();
					})
		));
		
		this.list.addSmall(
			Button.builder(
				Component.translatable("controls.reset"), _ -> {
					capes.reset();
					
					// Recreate screen
					this.minecraft.setScreen(new AdvancedMenuScreen(this.lastScreen, this.options));
				}).build(),
			null);
	}
	
	private Component requiresRestartComponent()
	{
		return Component.literal("\nRequires restart to take effect")
			.withStyle(ChatFormatting.ITALIC);
	}
	
	@Override
	public void onClose()
	{
		super.onClose();
		Capes.instance().refreshIfMarked();
	}
}
