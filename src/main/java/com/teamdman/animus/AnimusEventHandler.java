package com.teamdman.animus;

import com.teamdman.animus.items.ItemFragmentHealing;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event handler for Animus mod
 * Handles passive healing from Fragment of Healing items
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnimusEventHandler {

    // Track healing cooldown per player
    private static final Map<UUID, Integer> healingCooldowns = new HashMap<>();

    /**
     * Passive healing from Fragment of Healing
     * Runs every tick for each player
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only run on server side, at end of tick
        if (event.side.isClient() || event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        UUID playerId = player.getUUID();

        // Count healing fragments in inventory
        int fragmentCount = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == AnimusItems.FRAGMENT_HEALING.get()) {
                fragmentCount += stack.getCount();
            }
        }

        // No fragments = no healing
        if (fragmentCount == 0) {
            healingCooldowns.remove(playerId);
            return;
        }

        // Get current cooldown
        int currentCooldown = healingCooldowns.getOrDefault(playerId, 0);

        if (currentCooldown <= 0) {
            // Time to heal!
            // Only heal if not at full health
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0F);
            }

            // Reset cooldown based on fragment count
            int healingInterval = ItemFragmentHealing.getHealingInterval(fragmentCount);
            healingCooldowns.put(playerId, healingInterval);
        } else {
            // Decrement cooldown
            healingCooldowns.put(playerId, currentCooldown - 1);
        }
    }

    /**
     * Clean up player data when they log out
     */
    @SubscribeEvent
    public static void onPlayerLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        healingCooldowns.remove(event.getEntity().getUUID());
    }
}
