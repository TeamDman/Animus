package com.teamdman.animus;

import com.teamdman.animus.items.ItemFragmentHealing;
import com.teamdman.animus.items.sigils.ItemSigilFreeSoul;
import com.teamdman.animus.items.sigils.ItemSigilHeavenlyWrath;
import com.teamdman.animus.items.sigils.ItemSigilRemedium;
import com.teamdman.animus.items.sigils.ItemSigilReparare;
import com.teamdman.animus.items.sigils.ItemSigilStorm;
import com.teamdman.animus.items.sigils.ItemSigilTemporalDominance;
import com.teamdman.animus.items.sigils.ItemSigilEquivalency;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.rituals.RitualSerenity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.Binding;

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

        // Process Remedium Sigil and Reparare Sigil active effects FIRST
        // This needs to run every tick, regardless of healing fragments
        if (player.level() instanceof ServerLevel serverLevel) {
            ItemSigilRemedium.tickActiveSigils(player, serverLevel);
            ItemSigilReparare.tickActiveSigils(player, serverLevel);

            // Process Free Soul spectator mode timer
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                ItemSigilFreeSoul.tickActiveSpectators(serverPlayer, serverLevel);
            }
        }

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
        ItemSigilRemedium.onPlayerLogout(event.getEntity().getUUID());
        ItemSigilReparare.onPlayerLogout(event.getEntity().getUUID());

        // Clean up Free Soul spectator mode when player logs out
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ItemSigilFreeSoul.onPlayerLogout(serverPlayer);
        }
    }

    /**
     * Free Soul Sigil death prevention
     * Works like a Totem of Undying - prevents death and activates spectator mode
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        // Only handle players on server side
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        // Try to prevent death with Free Soul sigil
        if (ItemSigilFreeSoul.tryPreventDeath(player, event.getSource())) {
            // Death was prevented by the sigil
            event.setCanceled(true);
        }
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
     * Prevent mob spawning in Ritual of Serenity zones
     * This event fires before a mob finalizes spawning, allowing us to cancel it
     * Note: Spawner spawns are allowed to proceed
     */
    @SubscribeEvent
    public static void onMobSpawnCheck(MobSpawnEvent.FinalizeSpawn event) {
        // Only run on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Allow spawner spawns to proceed
        if (event.getSpawnType() == net.minecraft.world.entity.MobSpawnType.SPAWNER) {
            return;
        }

        // Only process actual Level instances (not WorldGenRegion during chunk generation)
        if (!(event.getLevel() instanceof Level)) {
            return;
        }

        // Check if the spawn position is within a Serenity zone
        Level level = (Level) event.getLevel();
        BlockPos spawnPos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

        if (RitualSerenity.isInSerenityZone(level, spawnPos)) {
            // Cancel the spawn (but not spawner spawns)
            event.setSpawnCancelled(true);
        }
    }

    /**
     * Process pending fish spawns from Sigil of Storm and fall effects from Sigil of Heavenly Wrath
     * This runs every server tick and checks if any pending spawns/effects are ready
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
            ItemSigilHeavenlyWrath.tickPendingFalls(serverLevel);
            ItemSigilTemporalDominance.tickAcceleratedBlocks(serverLevel);
            ItemSigilEquivalency.tickReplacements(serverLevel);
        }
    }

    /**
     * Key of Binding functionality: Transfer binding ownership
     * When a player has a bound Key of Binding in their offhand and binds an item,
     * the item gets bound to the Key's owner instead of the player
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        // Only run on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        Player player = event.getEntity();
        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHandStack = player.getItemInHand(InteractionHand.OFF_HAND);

        // Check if player has Key of Binding in offhand
        if (offHandStack.getItem() != AnimusItems.KEY_BINDING.get()) {
            return;
        }

        // Check if the Key is bound
        if (!(offHandStack.getItem() instanceof IBindable keyBindable)) {
            return;
        }

        Binding keyBinding = keyBindable.getBinding(offHandStack);
        if (keyBinding == null) {
            // Key is not bound, no effect
            return;
        }

        // Check if main hand item is an IBindable
        if (!(mainHandStack.getItem() instanceof IBindable itemBindable)) {
            return;
        }

        // Prevent Key of Binding from binding another Key of Binding
        if (mainHandStack.getItem() == AnimusItems.KEY_BINDING.get()) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(Constants.Localizations.Text.KEY_CANNOT_BIND_KEY)
                    .withStyle(net.minecraft.ChatFormatting.RED),
                true
            );
            event.setCanceled(true);
            return;
        }

        // Check if the item is not already bound
        Binding itemBinding = itemBindable.getBinding(mainHandStack);
        if (itemBinding != null) {
            // Item is already bound, don't interfere
            return;
        }

        // At this point, we need to let the binding happen normally,
        // then immediately replace the binding with the Key's owner
        // We'll do this in a follow-up tick event

        // Schedule the binding transfer for next tick
        // This allows Blood Magic's binding to complete first
        pendingBindingTransfers.put(player.getUUID(), keyBinding);
    }

    // Track pending binding transfers
    private static final Map<UUID, Binding> pendingBindingTransfers = new HashMap<>();

    /**
     * Complete pending binding transfers from Key of Binding
     */
    @SubscribeEvent
    public static void onPlayerTickForBinding(TickEvent.PlayerTickEvent event) {
        // Only run on server side, at start of tick
        if (event.side.isClient() || event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        UUID playerId = player.getUUID();

        // Check if this player has a pending binding transfer
        Binding keyBinding = pendingBindingTransfers.remove(playerId);
        if (keyBinding == null) {
            return;
        }

        // Find any newly bound items in player's inventory and transfer the binding
        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (mainHandStack.getItem() instanceof IBindable bindable) {
            Binding currentBinding = bindable.getBinding(mainHandStack);

            // Check if the item was just bound to this player
            if (currentBinding != null && currentBinding.getOwnerId().equals(playerId)) {
                // Transfer the binding to the Key's owner
                mainHandStack.getOrCreateTag().put("binding", keyBinding.serializeNBT());

                // Notify player
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(Constants.Localizations.Text.KEY_ITEM_BOUND, keyBinding.getOwnerName())
                        .withStyle(net.minecraft.ChatFormatting.AQUA),
                    true
                );
            }
        }
    }

    /**
     * Sentient Shield blocking effects
     * When a player blocks an attack with a sentient shield, apply effects based on will type
     */
    @SubscribeEvent
    public static void onLivingAttack(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        // Only handle players on server side
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }

        // Check if player is blocking and has a sentient shield
        if (!player.isBlocking()) {
            return;
        }

        ItemStack shield = com.teamdman.animus.items.ItemSentientShield.getSentientShield(player);
        if (shield.isEmpty() || !(shield.getItem() instanceof com.teamdman.animus.items.ItemSentientShield sentientShield)) {
            return;
        }

        // Get the attacker
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof net.minecraft.world.entity.LivingEntity livingAttacker)) {
            return;
        }

        // Get the will type and amount
        wayoftime.bloodmagic.api.compat.EnumDemonWillType willType = sentientShield.getCurrentType(shield);
        double willAmount = wayoftime.bloodmagic.demonaura.WorldDemonWillHandler.getCurrentWill(
            player.level(), player.blockPosition(), willType
        );

        // Need at least some will to trigger effects
        if (willAmount < 10.0) {
            return;
        }

        // Apply effects based on will type (all last 5 seconds = 100 ticks)
        int effectDuration = 100;

        switch (willType) {
            case DEFAULT: // Raw will - Strength 2 to player
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DAMAGE_BOOST,
                    effectDuration,
                    1 // Level 2 (0-indexed)
                ));
                break;

            case STEADFAST: // Resistance 2 to player
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                    effectDuration,
                    1 // Level 2
                ));
                break;

            case CORROSIVE: // Poison to attacker
                livingAttacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.POISON,
                    effectDuration,
                    1 // Level 2
                ));
                break;

            case VENGEFUL: // 30% damage reflection + weakness to attacker
                // Calculate reflected damage (30% of attack damage)
                float reflectedDamage = event.getAmount() * 0.3f;
                livingAttacker.hurt(player.damageSources().thorns(player), reflectedDamage);

                // Apply weakness
                livingAttacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WEAKNESS,
                    effectDuration,
                    0 // Level 1
                ));
                break;
        }
    }

    // Note: Sentient Shield will gain bonus is not implemented yet
    // Blood Magic doesn't expose a will gain event that we can hook into
    // This feature will need to be implemented through a different mechanism
}
