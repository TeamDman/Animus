package com.teamdman_9201.nova.gui;

import com.teamdman_9201.nova.NOVA;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;

import java.util.ArrayList;
import java.util.List;

import static com.teamdman_9201.nova.NOVAConfig.config;

/**
 * Created by TeamDman on 2015-05-14.
 */
public class ConfigGui extends cpw.mods.fml.client.config.GuiConfig {

    public ConfigGui(GuiScreen parent) {
        super(parent, getConfigElements(parent), NOVA.MODID, false, false, NOVA.MODID);
    }

    @SuppressWarnings("rawtypes")
    private static List<IConfigElement> getConfigElements(GuiScreen parent) {
        List<IConfigElement> list = new ArrayList<IConfigElement>();

        // adds sections declared in ConfigHandler. toLowerCase() is used because the
        // configuration class automatically does this, so must we.
        list.add(new ConfigElement<ConfigCategory>(config.getCategory("general")));
        list.add(new ConfigElement<ConfigCategory>(config.getCategory("blacklist")));
        list.add(new ConfigElement<ConfigCategory>(config.getCategory("enchantments")));
        list.add(new ConfigElement<ConfigCategory>(config.getCategory("ritual blacklist")));
        list.add(new ConfigElement<ConfigCategory>(config.getCategory("ritual costs")));
        list.add(new ConfigElement<ConfigCategory>(config.getCategory("ritual levels")));

        return list;
    }

}
