package com.teamdman.animus.client;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.sigils.ItemSigilEquivalency;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Client-side handler for mouse scroll events on sigils
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.Mod.MODID)
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
            if (sigil.onScroll(player, mainHand, event.getScrollDeltaY(), InteractionHand.MAIN_HAND)) {
                event.setCanceled(true);
                return;
            }
        }

        // Check off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof ItemSigilEquivalency sigil) {
            if (sigil.onScroll(player, offHand, event.getScrollDeltaY(), InteractionHand.OFF_HAND)) {
                event.setCanceled(true);
            }
        }
    }
}
