package com.teamdman_9201.nova.handlers;


import com.teamdman_9201.nova.NOVA;
import com.teamdman_9201.nova.NOVAConfig;

import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class NOVAEventHandler {

    @SubscribeEvent
    public void onClientChatRecieved(ClientChatReceivedEvent event) {
        if (NOVA.doLowerChat) {
            event.message = new ChatComponentText(event.message.getFormattedText().toLowerCase());
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        if (eventArgs.modID.equals(NOVA.MODID)) {
            NOVAConfig.syncConfig();
            System.out.println("CONFIG SYNCED");
        }
    }

}
