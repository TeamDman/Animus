package com.teamdman_9201.nova.gui;

import com.teamdman_9201.nova.NOVA;
import com.teamdman_9201.nova.NOVAConfig;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

/**
 * Created by TeamDman on 2015-05-14.
 */
public class ConfigGui extends cpw.mods.fml.client.config.GuiConfig {

    public ConfigGui(GuiScreen parent) {
        super(parent, new ConfigElement(NOVAConfig.config.getCategory(Configuration
                .CATEGORY_GENERAL))
                .getChildElements(), NOVA.MODID, false, false, cpw.mods.fml.client.config
                .GuiConfig.getAbridgedConfigPath(NOVAConfig.config.toString()));
    }


}
