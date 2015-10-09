package com.teamdman_9201.nova.handlers;


import com.teamdman_9201.nova.NOVA;
import com.teamdman_9201.nova.NOVAConfig;
import com.teamdman_9201.nova.slots.SlotNope;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

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
        int frags = 0;
        for (int i = 0; i < open.inventorySlots.size(); i++) {
            Slot slot = (Slot) open.inventorySlots.get(i);
            boolean isFrag = (slot.getHasStack() && slot.getStack().getItem() == NOVA.itemHealingFragment);
            if (isFrag) {
                frags++;
                if (!eventArgs.player.capabilities.isCreativeMode && slot.getClass()==Slot.class) {
                    open.inventorySlots.set(i,new SlotNope(slot.inventory, slot.getSlotIndex(), slot.xDisplayPosition, slot.yDisplayPosition));
                }
            }
        }
        if (eventArgs.player.worldObj.getWorldTime()%20==0 && frags>=9 && !eventArgs.player.worldObj.isRemote) {
            PotionEffect regen = new PotionEffect(Potion.regeneration.getId(),200,(int) frags/10);
            eventArgs.player.addPotionEffect(regen);
            if (frags>=36 && eventArgs.player.worldObj.getWorldTime()%200==0)
                eventArgs.player.addPotionEffect(new PotionEffect(Potion.field_76444_x.id,200,4));
        }
    }

}
