package com.TeamDman.nova;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.Set;

import cpw.mods.fml.client.IModGuiFactory;

/**
 * Created by TeamDman on 2015-05-14.
 */
public class NOVAGuiFactory  implements IModGuiFactory {
  @Override
  public void initialize(Minecraft minecraftInstance) {

  }

  @Override
  public Class<? extends GuiScreen> mainConfigGuiClass() {
    return NOVAConfigGui.class;
  }

  @Override
  public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
    return null;
  }

  @Override
  public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
    return null;
  }
}
