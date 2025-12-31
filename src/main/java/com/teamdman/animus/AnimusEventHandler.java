package com.teamdman.animus;

import com.teamdman.animus.items.ItemFragmentHealing;
import com.teamdman.animus.items.sigils.ItemSigilFreeSoul;
import com.teamdman.animus.items.sigils.ItemSigilHeavenlyWrath;
import com.teamdman.animus.items.sigils.ItemSigilMonk;
import com.teamdman.animus.items.sigils.ItemSigilRemedium;
import com.teamdman.animus.items.sigils.ItemSigilReparare;
import com.teamdman.animus.items.sigils.ItemSigilStorm;
import com.teamdman.animus.items.sigils.ItemSigilTemporalDominance;
import com.teamdman.animus.items.sigils.ItemSigilEquivalency;
import com.teamdman.animus.items.ItemSpearSentient;
import com.teamdman.animus.items.ItemSentientBow;
import com.teamdman.animus.entities.EntityThrownSpear;
import com.teamdman.animus.entities.EntitySentientArrow;
import com.teamdman.animus.registry.AnimusAttributes;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusSounds;
import com.teamdman.animus.rituals.RitualSerenity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.api.compat.IDemonWill;
import wayoftime.bloodmagic.api.compat.IDemonWillWeapon;
import wayoftime.bloodmagic.common.item.BloodMagicItems;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.potion.BloodMagicPotions;
import wayoftime.bloodmagic.util.helper.NetworkHelper;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;
import net.minecraft.world.effect.MobEffectInstance;
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
import wayoftime.bloodmagic.anointment.AnointmentHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

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
     * Note: Spawner spawns and conversions (such as villager -> zombie villager) are allowed to proceed
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

        // Allow conversion spawns (such as villager -> zombie villager) to proceed
        // These fire FinalizeSpawn "late" after the entity is already in the world,
        // and calling setSpawnCancelled on them throws UnsupportedOperationException
        if (event.getSpawnType() == net.minecraft.world.entity.MobSpawnType.CONVERSION) {
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
            // Cancel the spawn (but not spawner spawns or conversions)
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

            // Sync accelerated blocks to clients every 10 ticks (0.5 seconds)
            if (serverLevel.getGameTime() % 10 == 0) {
                ItemSigilTemporalDominance.syncToClients(serverLevel);
            }
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

    // Constants for Sigil of the Demon Monk
    private static final double MIN_WILL_FOR_EXECUTE = 1.0;
    private static final double MAX_WILL_FOR_EXECUTE = 4096.0;
    private static final double MIN_EXECUTE_PERCENT = 0.01; // 1%
    private static final double DEFAULT_MAX_EXECUTE_PERCENT = 0.15; // 15% (fallback if config not loaded)
    private static final double WILL_COST_PER_EXECUTE = 5.0;
    private static final int LP_REWARD_PER_EXECUTE = 200;
    private static final int LP_COST_PER_ACTION = 5; // LP cost per hit or mine

    /**
     * Gets the max execute threshold from config with a fallback for pre-config-load access
     */
    private static double getMaxExecutePercent() {
        try {
            return AnimusConfig.sigils.monkExecuteThreshold.get();
        } catch (IllegalStateException e) {
            return DEFAULT_MAX_EXECUTE_PERCENT;
        }
    }

    /**
     * Sigil of the Monk - Apply unarmed damage bonus when attacking with empty hands
     * Also applies knockback to the target
     * Adds demon will scaling damage (1-15% of target max health based on will 1-4096)
     * Execute mechanic: if target falls below execute threshold, instant kill + rewards
     */
    @SubscribeEvent
    public static void onLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        // Check if damage source is from a player
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Must be a direct melee attack (player is the direct entity, not a projectile/spell)
        // This prevents spell damage from Iron's Spells from triggering the monk bonus
        if (event.getSource().getDirectEntity() != player) {
            return;
        }

        // Check if player is attacking with empty hands
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!ItemSigilMonk.hasActiveSigil(player)) {
            return;
        }

        // Get unarmed damage attribute value
        double unarmedDamage = player.getAttributeValue(AnimusAttributes.UNARMED_DAMAGE.get());
        if (unarmedDamage <= 0) {
            return;
        }

        net.minecraft.world.entity.LivingEntity target = event.getEntity();
        float targetMaxHealth = target.getMaxHealth();
        float baseDamage = event.getAmount() + (float) unarmedDamage;

        // Get player's total demon will (all types combined)
        double totalWill = getTotalDemonWill(player);

        // Calculate execute value based on demon will (1-15% of max health)
        // Linear scaling from 1% at 1 will to 15% at 4096 will
        double executePercent = 0;
        float willBonusDamage = 0;

        if (totalWill >= MIN_WILL_FOR_EXECUTE) {
            // Clamp will to max value for calculation
            double clampedWill = Math.min(totalWill, MAX_WILL_FOR_EXECUTE);

            // Linear interpolation: (will - min) / (max - min) * (maxPercent - minPercent) + minPercent
            double willRatio = (clampedWill - MIN_WILL_FOR_EXECUTE) / (MAX_WILL_FOR_EXECUTE - MIN_WILL_FOR_EXECUTE);
            executePercent = MIN_EXECUTE_PERCENT + willRatio * (getMaxExecutePercent() - MIN_EXECUTE_PERCENT);

            // Add bonus damage equal to executePercent of target's max health
            willBonusDamage = (float) (targetMaxHealth * executePercent);
        }

        float totalDamage = baseDamage + willBonusDamage;
        event.setAmount(totalDamage);

        // Calculate health after this attack
        float healthAfterAttack = target.getHealth() - totalDamage;

        // Execute threshold is executePercent of max health
        float executeThreshold = (float) (targetMaxHealth * executePercent);

        // Consume LP per hit
        ItemSigilMonk.consumeLP(player, LP_COST_PER_ACTION);

        // Check for execute: if health after attack is below execute threshold
        // Guaranteed execute if player has enough will
        if (executePercent > 0 && healthAfterAttack > 0 && healthAfterAttack < executeThreshold) {
            // Check if player has enough will to execute
            if (totalWill >= WILL_COST_PER_EXECUTE) {
                // Consume 5 will from the player (prefer raw will first)
                consumeDemonWill(player, WILL_COST_PER_EXECUTE);

                // Deal lethal damage (100% of max health)
                event.setAmount(targetMaxHealth * 2); // Overkill to ensure death

                // Play execute sound
                player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    AnimusSounds.EXECUTE.get(),
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f
                );

                // Spawn soul speed particles around the dying entity
                if (player.level() instanceof ServerLevel serverLevel) {
                    double x = target.getX();
                    double y = target.getY() + target.getBbHeight() / 2.0;
                    double z = target.getZ();
                    // Spawn a burst of soul particles
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SOUL,
                        x, y, z,
                        20, // count
                        0.5, 0.5, 0.5, // spread
                        0.1 // speed
                    );
                }

                // Add 200 LP to player's soul network
                SoulNetwork network = NetworkHelper.getSoulNetwork(player);
                if (network != null) {
                    network.add(new SoulTicket(
                        Component.translatable(Constants.Localizations.Text.TICKET_MONK_EXECUTE),
                        LP_REWARD_PER_EXECUTE
                    ), LP_REWARD_PER_EXECUTE);
                }
            }
        }

        // Apply knockback to the target
        double knockbackStrength = 0.5D; // Similar to knockback enchantment level 1
        double dx = player.getX() - target.getX();
        double dz = player.getZ() - target.getZ();
        target.knockback(knockbackStrength, dx, dz);

        // Apply Soul Snare effect (2 seconds, amplifier 1) for guaranteed will drops
        target.addEffect(new MobEffectInstance(BloodMagicPotions.SOUL_SNARE.get(), 40, 1));
    }

    /**
     * Check if an item is a catchable projectile item (arrow, spectral arrow, trident, firework)
     */
    private static boolean isCatchableProjectileItem(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.ARROW) ||
               stack.is(net.minecraft.world.item.Items.SPECTRAL_ARROW) ||
               stack.is(net.minecraft.world.item.Items.TRIDENT) ||
               stack.is(net.minecraft.world.item.Items.FIREWORK_ROCKET);
    }

    /**
     * Sigil of the Demon Monk - Catch projectiles when hit with empty main hand or holding a catchable projectile
     * Cancels damage, gives the projectile item to the player, plays sound and particles
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerHurtByProjectile(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        // Check if the entity being hurt is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check if player has empty main hand OR is holding a catchable projectile item
        ItemStack mainHandItem = player.getMainHandItem();
        boolean canCatch = mainHandItem.isEmpty() || isCatchableProjectileItem(mainHandItem);
        if (!canCatch) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!ItemSigilMonk.hasActiveSigil(player)) {
            return;
        }

        // Check if damage is from a projectile
        if (!(event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile)) {
            return;
        }

        // Determine what item to give the player based on projectile type
        ItemStack caughtItem = ItemStack.EMPTY;

        if (projectile instanceof net.minecraft.world.entity.projectile.Arrow) {
            // Check if it's a spectral arrow or regular arrow
            if (projectile instanceof net.minecraft.world.entity.projectile.SpectralArrow) {
                caughtItem = new ItemStack(net.minecraft.world.item.Items.SPECTRAL_ARROW);
            } else {
                caughtItem = new ItemStack(net.minecraft.world.item.Items.ARROW);
            }
        } else if (projectile instanceof net.minecraft.world.entity.projectile.ThrownTrident trident) {
            // Create a new trident item - we can't access the original easily
            caughtItem = new ItemStack(net.minecraft.world.item.Items.TRIDENT);
        } else if (projectile instanceof net.minecraft.world.entity.projectile.FireworkRocketEntity) {
            caughtItem = new ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET);
        }

        // If we identified a catchable projectile
        if (!caughtItem.isEmpty()) {
            // Cancel the damage
            event.setCanceled(true);

            // Add caught item to player's inventory (or increment stack if holding same type)
            if (mainHandItem.isEmpty()) {
                // Empty hand - put item directly in main hand
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, caughtItem);
            } else if (mainHandItem.is(caughtItem.getItem()) && mainHandItem.getCount() < mainHandItem.getMaxStackSize()) {
                // Holding same item type and stack isn't full - increment stack
                mainHandItem.grow(1);
            } else {
                // Holding different projectile type or stack is full - add to inventory
                if (!player.getInventory().add(caughtItem)) {
                    // Inventory full - drop item at player's feet
                    player.drop(caughtItem, false);
                }
            }

            // Remove the projectile entity
            projectile.discard();

            // Play a catch sound (using item pickup sound)
            player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                net.minecraft.sounds.SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                1.0f,
                0.8f + player.getRandom().nextFloat() * 0.4f
            );

            // Spawn breeze particles around the player
            if (player.level() instanceof ServerLevel serverLevel) {
                // Use white poof/cloud particles as breeze-like effect
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CLOUD,
                    player.getX(),
                    player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(),
                    15, // count
                    0.4, 0.4, 0.4, // spread
                    0.05 // speed
                );
                // Add some white particles for extra flair
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.POOF,
                    player.getX(),
                    player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(),
                    10, // count
                    0.3, 0.3, 0.3, // spread
                    0.02 // speed
                );
            }

            // Consume LP for the catch action (50 LP)
            ItemSigilMonk.consumeLP(player, 50);
        }
    }

    /**
     * Sigil of the Demon Monk - Cancel fall damage for 25 LP
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFallDamage(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        // Check if the entity is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check if this is fall damage
        if (!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!ItemSigilMonk.hasActiveSigil(player)) {
            return;
        }

        // Cancel fall damage and consume 25 LP
        event.setCanceled(true);
        ItemSigilMonk.consumeLP(player, 25);

        // Spawn some landing particles
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.CLOUD,
                player.getX(),
                player.getY(),
                player.getZ(),
                8,
                0.3, 0.1, 0.3,
                0.02
            );
        }
    }

    /**
     * Get the total demon will across all types for a player
     */
    private static double getTotalDemonWill(Player player) {
        double total = 0;
        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            total += PlayerDemonWillHandler.getTotalDemonWill(type, player);
        }
        return total;
    }

    /**
     * Consume demon will from the player, preferring raw will first
     */
    private static void consumeDemonWill(Player player, double amount) {
        double remaining = amount;

        // Try to consume from each will type, starting with DEFAULT (raw)
        EnumDemonWillType[] preferredOrder = {
            EnumDemonWillType.DEFAULT,
            EnumDemonWillType.VENGEFUL,
            EnumDemonWillType.STEADFAST,
            EnumDemonWillType.CORROSIVE,
            EnumDemonWillType.DESTRUCTIVE
        };

        for (EnumDemonWillType type : preferredOrder) {
            if (remaining <= 0) break;

            double available = PlayerDemonWillHandler.getTotalDemonWill(type, player);
            if (available > 0) {
                double toConsume = Math.min(remaining, available);
                PlayerDemonWillHandler.consumeDemonWill(type, player, toConsume);
                remaining -= toConsume;
            }
        }
    }

    /**
     * Sigil of the Monk - Grant knockback resistance and regeneration on kill
     * When killing an enemy with empty hands while sigil is active:
     * - Grants Knockback Resistance II for 5 seconds
     * - Grants Regeneration I for 2 seconds
     */
    @SubscribeEvent
    public static void onLivingDeath_Monk(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        // Check if killer is a player
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Must be a direct melee kill (player is the direct entity, not a projectile/spell)
        if (event.getSource().getDirectEntity() != player) {
            return;
        }

        // Check if player killed with empty hands
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!ItemSigilMonk.hasActiveSigil(player)) {
            return;
        }

        // Grant Resistance II for 5 seconds (100 ticks) - reduces damage taken
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
            100, // 5 seconds
            1    // Level 2 (0-indexed)
        ));

        // Grant Regeneration I for 2 seconds (40 ticks)
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            net.minecraft.world.effect.MobEffects.REGENERATION,
            40,  // 2 seconds
            0    // Level 1 (0-indexed)
        ));
    }

    // Netherite pickaxe base speed (9.0) + Efficiency V bonus
    // Efficiency formula: base_speed + (level^2 + 1) = 9.0 + (5^2 + 1) = 9.0 + 26 = 35.0
    private static final float MONK_MINING_SPEED = 35.0f;

    /**
     * Sigil of the Demon Monk - Grant netherite + efficiency 5 mining speed with empty hands
     */
    @SubscribeEvent
    public static void onBreakSpeed(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        // Check if player is mining with empty hands
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!ItemSigilMonk.hasActiveSigil(player)) {
            return;
        }

        // Grant netherite + efficiency 5 mining speed for all blocks
        // This treats the player's fist as a valid tool for any block
        event.setNewSpeed(MONK_MINING_SPEED);
    }

    /**
     * Sigil of the Demon Monk - Allow harvesting blocks that require tools when using empty hands
     * This makes the player's fist act like a valid tool for all block types
     */
    @SubscribeEvent
    public static void onHarvestCheck(net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        // Check if player is mining with empty hands
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!ItemSigilMonk.hasActiveSigil(player)) {
            return;
        }

        // Allow harvesting all blocks when Sigil of the Monk is active
        event.setCanHarvest(true);
    }

    /**
     * Sigil of the Demon Monk - Consume LP when breaking a block with empty hands
     */
    @SubscribeEvent
    public static void onBlockBreak(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check if player is mining with empty hands
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!ItemSigilMonk.hasActiveSigil(player)) {
            return;
        }

        // Consume LP per block broken
        ItemSigilMonk.consumeLP(player, LP_COST_PER_ACTION);
    }

    /**
     * Handle Blood Magic anointment bonus damage for thrown spears
     * Blood Magic's GenericHandler only handles melee attacks (checks player's held item)
     * This handler applies anointment damage bonuses when entities are hit by thrown spears
     */
    @SubscribeEvent
    public static void onThrownSpearHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();

        // Check if damage is from a thrown spear
        if (!(directEntity instanceof EntityThrownSpear thrownSpear)) {
            return;
        }

        // Get the spear item (use actual item for consistency)
        ItemStack spearStack = thrownSpear.getSpearItem();
        if (spearStack.isEmpty()) {
            return;
        }

        // Check if the spear has anointments
        AnointmentHolder holder = AnointmentHolder.fromItemStack(spearStack);
        if (holder == null) {
            return;
        }

        // Get the attacking player (if any)
        Player attackingPlayer = sourceEntity instanceof Player ? (Player) sourceEntity : null;

        // Apply anointment damage bonuses
        LivingEntity target = event.getEntity();
        double additionalDamage = holder.getAdditionalDamage(attackingPlayer, spearStack, event.getAmount(), target);
        if (additionalDamage > 0) {
            event.setAmount((float) (event.getAmount() + additionalDamage));
        }
    }

    /**
     * Consume anointment durability when thrown spear deals damage
     * This mirrors Blood Magic's GenericHandler.onLivingDamage behavior
     */
    @SubscribeEvent
    public static void onThrownSpearDamage(net.minecraftforge.event.entity.living.LivingDamageEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        Entity sourceEntity = event.getSource().getEntity();

        // Check if damage is from a thrown spear
        if (!(directEntity instanceof EntityThrownSpear thrownSpear)) {
            return;
        }

        // Only process on server side
        if (thrownSpear.level().isClientSide()) {
            return;
        }

        // Get the player who threw the spear
        if (!(sourceEntity instanceof Player player)) {
            return;
        }

        // Get the actual spear item (not a copy) so we can update anointments
        ItemStack spearStack = thrownSpear.getSpearItem();
        if (spearStack.isEmpty()) {
            return;
        }

        // Check if the spear has anointments
        AnointmentHolder holder = AnointmentHolder.fromItemStack(spearStack);
        if (holder == null) {
            return;
        }

        // Consume anointment durability
        // Note: We use MAINHAND as the slot since that's where the spear came from
        if (holder.consumeAnointmentDurabilityOnHit(spearStack, EquipmentSlot.MAINHAND, player)) {
            // Update the anointments on the actual stored item
            holder.toItemStack(spearStack);
        }
    }

    /**
     * Handle will drops from Sentient weapons (Spear melee/thrown, Bow arrows) and
     * Ritual of Endless Greed - Intercept mob drops and transfer to container
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDrops(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        net.minecraft.world.entity.LivingEntity killedEntity = event.getEntity();
        net.minecraft.world.level.Level level = killedEntity.level();

        // Only run on server side
        if (level.isClientSide()) {
            return;
        }

        // Handle Sentient weapon will drops (spear melee/thrown and bow arrows)
        handleSentientWeaponWillDrops(event);

        // Check if the entity died within range of an Endless Greed ritual
        if (com.teamdman.animus.rituals.RitualEndlessGreed.handleMobDrops(level, killedEntity.blockPosition(), event.getDrops())) {
            // Clear drops since they were handled (either collected or destroyed)
            event.getDrops().clear();
        }
    }

    /**
     * Handle demon will drops from Sentient weapon kills
     * Works for:
     * - Melee kills (player holding spear)
     * - Thrown spear kills (EntityThrownSpear)
     * - Sentient Bow arrow kills (EntitySentientArrow)
     * Matches the behavior of Blood Magic's Sentient Sword
     */
    private static void handleSentientWeaponWillDrops(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        net.minecraft.world.entity.LivingEntity killedEntity = event.getEntity();
        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        Entity sourceEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        // Check for thrown sentient spear kill
        if (directEntity instanceof EntityThrownSpear thrownSpear && "sentient".equals(thrownSpear.getVariant())) {
            // Get the player who threw the spear
            if (sourceEntity instanceof Player player) {
                // Get will type and level from the thrown spear
                EnumDemonWillType willType = thrownSpear.getWillType();
                int willLevel = Math.min(thrownSpear.getWillLevel(), 4);

                // Generate will drops using spear drop tables
                java.util.List<ItemStack> willDrops = generateSentientWillDrops(
                    killedEntity, player, willType, willLevel, event.getLootingLevel(),
                    ItemSpearSentient.soulDrop, ItemSpearSentient.staticDrop
                );

                // Try to add will directly to player's tartaric gem, drop excess
                addWillDropsToPlayerOrWorld(player, killedEntity, willDrops, event.getDrops());
            }
            return;
        }

        // Check for sentient bow arrow kill
        if (directEntity instanceof EntitySentientArrow sentientArrow) {
            // Get the player who shot the arrow
            if (sourceEntity instanceof Player player) {
                // Get will type and level from the arrow
                EnumDemonWillType willType = sentientArrow.getWillType();
                int willLevel = Math.min(sentientArrow.getWillLevel(), 4);

                // Generate will drops using bow drop tables
                java.util.List<ItemStack> willDrops = generateSentientWillDrops(
                    killedEntity, player, willType, willLevel, event.getLootingLevel(),
                    ItemSentientBow.soulDrop, ItemSentientBow.staticDrop
                );

                // Try to add will directly to player's tartaric gem, drop excess
                addWillDropsToPlayerOrWorld(player, killedEntity, willDrops, event.getDrops());
            }
            return;
        }

        // Check for melee sentient spear kill (player holding the spear)
        // Note: Blood Magic's WillHandler already handles IDemonWillWeapon for held weapons,
        // so this is just a backup in case that doesn't fire for our spear
        if (sourceEntity instanceof Player player) {
            ItemStack heldStack = player.getMainHandItem();
            if (heldStack.getItem() instanceof ItemSpearSentient sentientSpear) {
                // Generate will drops using the spear's method
                java.util.List<ItemStack> willDrops = sentientSpear.getRandomDemonWillDrop(
                    killedEntity, player, heldStack, event.getLootingLevel()
                );

                // Try to add will directly to player's tartaric gem, drop excess
                addWillDropsToPlayerOrWorld(player, killedEntity, willDrops, event.getDrops());
            }
        }
    }

    /**
     * Generate demon will drops for a sentient weapon kill
     */
    private static java.util.List<ItemStack> generateSentientWillDrops(
        net.minecraft.world.entity.LivingEntity killedEntity,
        Player attackingEntity,
        EnumDemonWillType willType,
        int willLevel,
        int looting,
        double[] soulDrop,
        double[] staticDrop
    ) {
        java.util.List<ItemStack> soulList = new java.util.ArrayList<>();

        // Only drop from hostile mobs (same check as Sentient Sword)
        if (killedEntity.getCommandSenderWorld().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && !(killedEntity instanceof net.minecraft.world.entity.monster.Enemy)) {
            return soulList;
        }

        // Slimes give reduced will
        double willModifier = killedEntity instanceof net.minecraft.world.entity.monster.Slime ? 0.67 : 1;

        // Get the appropriate demon will item based on will type
        IDemonWill soul = switch (willType) {
            case CORROSIVE -> ((IDemonWill) BloodMagicItems.MONSTER_SOUL_CORROSIVE.get());
            case DESTRUCTIVE -> ((IDemonWill) BloodMagicItems.MONSTER_SOUL_DESTRUCTIVE.get());
            case STEADFAST -> ((IDemonWill) BloodMagicItems.MONSTER_SOUL_STEADFAST.get());
            case VENGEFUL -> ((IDemonWill) BloodMagicItems.MONSTER_SOUL_VENGEFUL.get());
            default -> ((IDemonWill) BloodMagicItems.MONSTER_SOUL_RAW.get());
        };

        // Drop will items (with looting bonus like sword)
        for (int i = 0; i <= looting; i++) {
            if (i == 0 || attackingEntity.getCommandSenderWorld().random.nextDouble() < 0.4) {
                double dropAmount = willModifier * (soulDrop[willLevel] * attackingEntity.getCommandSenderWorld().random.nextDouble()
                    + staticDrop[willLevel]) * killedEntity.getMaxHealth() / 20.0;
                ItemStack soulStack = soul.createWill(dropAmount);
                soulList.add(soulStack);
            }
        }

        return soulList;
    }

    /**
     * Try to add will drops directly to player's tartaric gem, drop excess as items
     * Matches Blood Magic's WillHandler behavior
     */
    private static void addWillDropsToPlayerOrWorld(
        Player player,
        net.minecraft.world.entity.LivingEntity killedEntity,
        java.util.List<ItemStack> willDrops,
        java.util.Collection<ItemEntity> existingDrops
    ) {
        if (willDrops.isEmpty()) {
            return;
        }

        for (ItemStack willStack : willDrops) {
            // Try to add directly to player's tartaric gem
            ItemStack remainder = PlayerDemonWillHandler.addDemonWill(player, willStack);

            // If gem is full or can't hold more, drop as item entity
            if (!remainder.isEmpty()) {
                EnumDemonWillType pickupType = ((IDemonWill) remainder.getItem()).getType(remainder);
                if (((IDemonWill) remainder.getItem()).getWill(pickupType, remainder) >= 0.0001) {
                    existingDrops.add(new ItemEntity(
                        killedEntity.getCommandSenderWorld(),
                        killedEntity.getX(),
                        killedEntity.getY(),
                        killedEntity.getZ(),
                        remainder
                    ));
                }
            }
        }

        // Sync inventory changes
        player.inventoryMenu.broadcastChanges();
    }
}
