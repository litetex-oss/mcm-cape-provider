package net.litetex.capes.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.litetex.capes.menu.preview.PreviewMenuScreen;
import net.minecraft.client.Minecraft;


public class ModMenuCompatibility implements ModMenuApi
{
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory()
	{
		return s -> new PreviewMenuScreen(s, Minecraft.getInstance().options);
	}
}
