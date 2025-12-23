package net.litetex.capes.menu.other;

import java.util.List;

import net.litetex.capes.Capes;
import net.litetex.capes.config.AnimatedCapesHandling;
import net.litetex.capes.menu.MainMenuScreen;
import net.minecraft.ChatFormatting;
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
					Component.literal("Only load your cape"),
					(btn, enabled) -> {
						this.config().setOnlyLoadForSelf(enabled);
						capes.saveConfigAndMarkRefresh();
					}),
			CycleButton.<AnimatedCapesHandling>builder(handling ->
					switch(handling)
					{
						case ON -> CommonComponents.OPTION_ON;
						case FROZEN -> Component.literal("Frozen");
						case OFF -> CommonComponents.OPTION_OFF;
					},
					this.config()::getAnimatedCapesHandling)
				.withValues(AnimatedCapesHandling.values())
				.create(
					Component.literal("Animated textures"),
					(btn, value) -> {
						this.config().setAnimatedCapesHandling(value);
						capes.saveConfigAndMarkRefresh();
					}),
			CycleButton.onOffBuilder(capes.config().isEnableElytraTexture())
				.create(
					Component.literal("Elytra texture"),
					(btn, enabled) -> {
						this.config().setEnableElytraTexture(enabled);
						capes.saveConfig();
					}),
			CycleButton.onOffBuilder(capes.config().isActivateExternalProvidersOnInitialLoad())
				.withTooltip(value -> Tooltip.create(Component.empty()
					.append(Component.literal("Should external providers (mods/local) be automatically "
						+ "activated/enabled when initially loaded?\n"))
					.append(this.requiresRestartComponent())))
				.create(
					Component.literal("Activate external providers"),
					(btn, enabled) -> {
						this.config().setActivateExternalProvidersOnInitialLoad(enabled);
						capes.saveConfig();
					}),
			CycleButton.onOffBuilder(capes.config().isLoadProvidersFromMods())
				.withTooltip(value -> Tooltip.create(Component.empty()
					.append("Should providers be loaded from other mods?\n")
					.append(this.requiresRestartComponent())))
				.create(
					Component.literal("Load Mod providers"),
					(btn, enabled) -> {
						this.config().setLoadProvidersFromMods(enabled);
						capes.saveConfig();
					}),
			CycleButton.onOffBuilder(capes.config().isLoadSimpleLocalProvidersFromFilesystem())
				.withTooltip(value -> Tooltip.create(Component.empty()
					.append("Should simple-local providers be loaded from "
						+ "config/cape-provider or config/cape-provider/simple-custom/...?\n")
					.append(this.requiresRestartComponent())))
				.create(
					Component.literal("Load local providers"),
					(btn, enabled) -> {
						this.config().setLoadSimpleLocalProvidersFromFilesystem(enabled);
						capes.saveConfig();
					})
		));
		
		this.list.addSmall(
			Button.builder(
				Component.translatable("controls.reset"), btn -> {
					capes.reset();
					
					// Recreate screen
					this.minecraft.setScreen(new OtherMenuScreen(this.lastScreen, this.options));
				}).build(),
			null);
	}
	
	private Component requiresRestartComponent()
	{
		return Component.literal("\nRequires restart to take effect")
			.withStyle(ChatFormatting.ITALIC);
	}
}
