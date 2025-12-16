package com.teamdman.animus.client;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.sigils.effects.EquivalencySigilEffect;
import com.teamdman.animus.network.AnimusPayloads;
import com.teamdman.animus.network.SigilRadiusPayload;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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

        if (player == null || !player.isShiftKeyDown()) {
            return;
        }

        // Check main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(AnimusItems.SIGIL_EQUIVALENCY.get())) {
            if (handleScroll(player, mainHand, event.getScrollDeltaY(), InteractionHand.MAIN_HAND)) {
                event.setCanceled(true);
                return;
            }
        }

        // Check off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.is(AnimusItems.SIGIL_EQUIVALENCY.get())) {
            if (handleScroll(player, offHand, event.getScrollDeltaY(), InteractionHand.OFF_HAND)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Handle scroll event for Equivalency sigil radius adjustment.
     * Returns true if scroll was handled.
     */
    private static boolean handleScroll(Player player, ItemStack stack, double scrollDelta, InteractionHand hand) {
        int currentRadius = EquivalencySigilEffect.getRadiusStatic(stack);
        int newRadius;

        if (scrollDelta > 0) {
            newRadius = Math.min(currentRadius + 1, EquivalencySigilEffect.getMaxRadius());
        } else {
            newRadius = Math.max(currentRadius - 1, EquivalencySigilEffect.getMinRadius());
        }

        if (newRadius != currentRadius) {
            // Update client-side immediately for responsiveness
            EquivalencySigilEffect.setRadius(stack, newRadius);

            // Send packet to server
            AnimusPayloads.sendToServer(new SigilRadiusPayload(hand, newRadius));

            // Display message
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.EQUIVALENCY_RADIUS, newRadius)
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
            return true;
        }

        return false;
    }
}
