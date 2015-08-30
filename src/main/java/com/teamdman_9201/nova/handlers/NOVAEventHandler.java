package com.teamdman_9201.nova.handlers;


import com.teamdman_9201.nova.NOVA;
import com.teamdman_9201.nova.NOVAConfig;
import com.teamdman_9201.nova.slots.SlotNope;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class NOVAEventHandler {

    @SubscribeEvent
    public void onClientChatRecieved(ClientChatReceivedEvent event) {
        if (NOVA.doLowerChat && !event.message.getUnformattedText().contains("http")) {
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

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent eventArgs) {
        Container open = eventArgs.player.openContainer;
        if (open == null)
            return;
        for (int i = 0; i < open.inventorySlots.size(); i++) {
            Slot slot = (Slot) open.inventorySlots.get(i);
            if (!(slot instanceof SlotNope) && slot.getHasStack() && slot.getStack().getItem() == NOVA.itemRedundantOrb) {
                Slot nope = new SlotNope(slot.inventory,slot.getSlotIndex(),slot.xDisplayPosition,slot.yDisplayPosition);
                open.inventorySlots.set(i,nope);
            }
        }
    }

}
