package net.litetex.capes.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.litetex.capes.menu.provider.ProviderMenuScreen;
import net.minecraft.client.Minecraft;


public class ModMenuCompatibility implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return s -> new ProviderMenuScreen(s, Minecraft.getInstance().options);
	}
}
