package com.TeamDman.nova;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.client.config.GuiConfig;

/**
 * Created by TeamDman on 2015-05-14.
 */
public class NOVAConfigGui extends GuiConfig {

  public NOVAConfigGui(GuiScreen parent) {
    super(parent, new ConfigElement(NOVA.config.getCategory(Configuration.CATEGORY_GENERAL))
              .getChildElements(), NOVA.MODID, false, false,
          GuiConfig.getAbridgedConfigPath(NOVA.config.toString()));
  }


}
