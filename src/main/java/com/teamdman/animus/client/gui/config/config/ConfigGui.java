package com.teamdman.animus.client.gui.config.config;

import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.Constants;
import com.teamdman.animus.Animus;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.ArrayList;
import java.util.List;

public class ConfigGui extends GuiConfig
{

    public ConfigGui(GuiScreen parentScreen)
    {
        super(parentScreen, getConfigElements(parentScreen), Animus.MODID, false, false, "Animus Configuration");
    }

    @SuppressWarnings("rawtypes")
    private static List<IConfigElement> getConfigElements(GuiScreen parent)
    {
        List<IConfigElement> list = new ArrayList<IConfigElement>();

        list.add(new ConfigElement(ConfigHandler.getConfig().getCategory("Item/Block Blacklisting".toLowerCase())));
//        list.add(new ConfigElement(ConfigHandler.getConfig().getCategory("Rituals".toLowerCase())));

        return list;
    }
}