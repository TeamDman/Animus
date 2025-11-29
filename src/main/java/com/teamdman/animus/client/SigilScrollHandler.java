package com.teamdman.animus.client;

import com.teamdman.animus.items.sigils.ItemSigilEquivalency;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side handler for mouse scroll events on sigils
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SigilScrollHandler {

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            return;
        }

        // Check main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof ItemSigilEquivalency sigil) {
            if (sigil.onScroll(player, mainHand, event.getScrollDelta())) {
                event.setCanceled(true);
                return;
            }
        }

        // Check off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof ItemSigilEquivalency sigil) {
            if (sigil.onScroll(player, offHand, event.getScrollDelta())) {
                event.setCanceled(true);
            }
        }
    }
}
