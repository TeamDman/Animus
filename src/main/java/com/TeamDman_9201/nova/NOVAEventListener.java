package com.TeamDman.nova;


import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class NOVAEventListener {

  @SubscribeEvent
  public void onClientChatRecieved(ClientChatReceivedEvent event) {
    if (NOVA.doLowerChat) {
      event.message = new ChatComponentText(event.message.getFormattedText().toLowerCase());
    }
  }

  @SubscribeEvent
  public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
    System.out.println(eventArgs.modID);
    if (eventArgs.modID.equals(NOVA.MODID)) {
      NOVA.syncConfig();
    }
  }
}
