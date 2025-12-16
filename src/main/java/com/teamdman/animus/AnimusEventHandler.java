package com.teamdman.animus;

import com.teamdman.animus.events.FragmentHealingEventHandler;
import com.teamdman.animus.items.sigils.effects.FreeSoulSigilEffect;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.util.SigilStateCleanupManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

/**
 * Central event handler for Animus mod
 * Handles player cleanup, Free Soul sigil death prevention, and ritual drop interception
 *
 * Other event handlers in the events package:
 * - MonkSigilEventHandler: Sigil of the Demon Monk functionality
 * - KeyBindingEventHandler: Key of Binding transfer functionality
 * - SentientShieldEventHandler: Sentient Shield blocking effects
 * - FragmentHealingEventHandler: Fragment of Healing passive healing
 * - WorldEffectEventHandler: Lightning conversion, spawn prevention, level ticks
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class AnimusEventHandler {

    /**
     * Process Free Soul spectator mode timer
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        // Only run on server side
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();

        // Process Free Soul spectator mode timer
        if (player.level() instanceof ServerLevel serverLevel) {
            if (player instanceof ServerPlayer serverPlayer) {
                FreeSoulSigilEffect.tickActiveSpectators(serverPlayer, serverLevel);
            }
        }
    }

    /**
     * Clean up player data when they log out
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerId = event.getEntity().getUUID();

        // Clean up healing cooldown
        FragmentHealingEventHandler.cleanupPlayer(playerId);

        // Clean up all registered sigil state trackers and custom handlers
        SigilStateCleanupManager.cleanupPlayer(playerId);

        // Clean up Free Soul spectator mode (needs ServerPlayer, not just UUID)
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FreeSoulSigilEffect.onPlayerLogout(serverPlayer);
        }
    }

    /**
     * Free Soul Sigil death prevention
     * Works like a Totem of Undying - prevents death and activates spectator mode
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        // Only handle players on server side
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        // Find Free Soul sigil in player's inventory
        ItemStack freeSoulStack = findFreeSoulSigil(player);
        if (freeSoulStack.isEmpty()) {
            return;
        }

        // Try to prevent death with Free Soul sigil
        if (FreeSoulSigilEffect.tryPreventDeath(player, event.getSource(), freeSoulStack)) {
            event.setCanceled(true);
        }
    }

    /**
     * Find a Free Soul sigil in the player's inventory.
     */
    private static ItemStack findFreeSoulSigil(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_FREE_SOUL.get())) {
                return stack;
            }
        }
        // Also check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_FREE_SOUL.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Ritual of Endless Greed - Intercept mob drops and transfer to container
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(LivingDropsEvent event) {
        net.minecraft.world.entity.LivingEntity entity = event.getEntity();
        net.minecraft.world.level.Level level = entity.level();

        // Only run on server side
        if (level.isClientSide()) {
            return;
        }

        // Check if the entity died within range of an Endless Greed ritual
        if (com.teamdman.animus.rituals.RitualEndlessGreed.handleMobDrops(level, entity.blockPosition(), event.getDrops())) {
            // Clear drops since they were handled
            event.getDrops().clear();
        }
    }
}
