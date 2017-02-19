package com.teamdman.animus.client.gui.config;


import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class ConfigGui extends GuiConfig {

	public ConfigGui(GuiScreen parentScreen) {
		super(parentScreen, getConfigElements(parentScreen), Animus.MODID, false, false, Animus.MODID);
	}

	private static List<IConfigElement> getConfigElements(GuiScreen parent) {
		List<IConfigElement> list = new ArrayList<IConfigElement>();

		list.add(new ConfigElement(AnimusConfig.getConfig().getCategory("Item/Block Blacklisting".toLowerCase())));
		list.add(new ConfigElement(AnimusConfig.getConfig().getCategory("Rituals".toLowerCase())));
		list.add(new ConfigElement(AnimusConfig.getConfig().getCategory("Sigil Variables".toLowerCase())));
		list.add(new ConfigElement(AnimusConfig.getConfig().getCategory("General".toLowerCase())));

		return list;
	}
}