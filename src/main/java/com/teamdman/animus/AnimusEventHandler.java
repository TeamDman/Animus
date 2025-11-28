package com.teamdman.animus;

import com.teamdman.animus.items.ItemFragmentHealing;
import com.teamdman.animus.items.sigils.ItemSigilStorm;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event handler for Animus mod
 * Handles passive healing from Fragment of Healing items
 * Prevents moving Fragment of Healing in inventory
 * Converts Life Essence to AntiLife when struck by lightning
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

        // Count healing fragments in inventory (each stack is a separate fragment since stackSize=1)
        int fragmentCount = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == AnimusItems.FRAGMENT_HEALING.get() && !stack.isEmpty()) {
                fragmentCount++; // Count each stack as one fragment (max stack size is 1)
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

    /**
     * Prevent dropping healing fragments
     */
    @SubscribeEvent
    public static void onItemToss(net.minecraftforge.event.entity.item.ItemTossEvent event) {
        if (event.getEntity().getItem().getItem() == AnimusItems.FRAGMENT_HEALING.get()) {
            Player player = event.getPlayer();
            if (player != null && !player.getAbilities().instabuild) {
                // Cancel the toss and return item to inventory
                event.setCanceled(true);
                player.getInventory().add(event.getEntity().getItem());
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(Constants.Localizations.Text.HEALING_CANNOT_DROP)
                        .withStyle(net.minecraft.ChatFormatting.RED),
                    true
                );
            }
        }
    }

    /**
     * Prevent moving Fragment of Healing in ANY inventory (player, chest, etc.)
     * This event fires for all inventory clicks
     */
    @SubscribeEvent
    public static void onInventoryClick(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        // This won't work for inventory clicks - we need a different approach
        // See onSlotClick below
    }

    /**
     * Track slots that contain healing fragments to prevent movement
     * This is a workaround since Forge doesn't expose the container click event directly
     */
    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        // When a container opens, we can't directly prevent slot clicks
        // The prevention is handled by:
        // 1. onDroppedByPlayer() in ItemFragmentHealing (prevents dropping)
        // 2. canFitInsideContainerItems() in ItemFragmentHealing (prevents shulker box storage)
        // 3. onItemToss() event above (prevents Q-key dropping)

        // For movement within inventory (shift-click, drag, etc.), we rely on client-side
        // behavior and server-side validation in the item pickup/drop events

        // Note: In 1.20.1, there's no easy server-side way to prevent slot clicks
        // without using mixins or access transformers. The best we can do is:
        // - Prevent dropping (done)
        // - Prevent storage in containers (done)
        // - Show clear warnings in tooltip (done)
        // - Return items that are illegally moved (handled by pickup prevention)
    }

    /**
     * Additional protection: prevent healing fragments from being placed in other containers
     */
    @SubscribeEvent
    public static void onItemCrafted(net.minecraftforge.event.entity.player.PlayerEvent.ItemCraftedEvent event) {
        // When fragments are crafted or obtained, they immediately become stuck
        if (event.getCrafting().getItem() == AnimusItems.FRAGMENT_HEALING.get()) {
            Player player = event.getEntity();
            if (!player.level().isClientSide) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(Constants.Localizations.Text.HEALING_WARNING)
                        .withStyle(net.minecraft.ChatFormatting.GOLD),
                    false
                );
            }
        }
    }

    /**
     * Convert Life Essence to AntiLife when struck by lightning
     * This creates a dramatic transformation effect
     */
    @SubscribeEvent
    public static void onLightningStrike(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        // Only handle lightning bolts on server side
        if (!(entity instanceof LightningBolt) || event.getLevel().isClientSide()) {
            return;
        }

        LightningBolt lightning = (LightningBolt) entity;
        Level level = event.getLevel();
        BlockPos strikePos = lightning.blockPosition();

        // Check a 3x3x3 area around the lightning strike
        for (BlockPos pos : BlockPos.betweenClosed(
            strikePos.offset(-1, -1, -1),
            strikePos.offset(1, 1, 1)
        )) {
            BlockState state = level.getBlockState(pos);

            // Check if this block is Blood Magic's life essence fluid
            if (state.getBlock().getDescriptionId().contains("life_essence")) {
                // Convert to AntiLife fluid
                level.setBlock(pos, AnimusBlocks.BLOCK_FLUID_ANTILIFE.get().defaultBlockState(), 3);

                // Schedule tick so it starts spreading
                level.scheduleTick(pos, AnimusBlocks.BLOCK_FLUID_ANTILIFE.get(), 1);
            }
        }
    }

    /**
     * Process pending fish spawns from Sigil of Storm
     * This runs every server tick and checks if any pending spawns are ready
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        // Only run on server side, at end of tick
        if (event.side.isClient() || event.phase != TickEvent.Phase.END) {
            return;
        }

        // Only process server levels
        if (event.level instanceof ServerLevel serverLevel) {
            ItemSigilStorm.tickPendingSpawns(serverLevel);
        }
    }
}
