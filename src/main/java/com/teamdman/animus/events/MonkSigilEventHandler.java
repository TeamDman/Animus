package com.teamdman.animus.events;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.sigils.effects.MonkSigilEffect;
import com.teamdman.animus.registry.AnimusAttributes;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.common.datacomponent.SoulNetwork;
import com.breakinblocks.neovitae.common.effect.BMMobEffects;
import com.breakinblocks.neovitae.util.helper.SoulNetworkHelper;
import com.breakinblocks.neovitae.will.PlayerDemonWillHandler;

/**
 * Event handler for Sigil of the Demon Monk functionality
 * Handles unarmed combat bonuses, projectile catching, fall damage negation,
 * mining speed boost, and kill rewards
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class MonkSigilEventHandler {

    // Constants for execute mechanic
    private static final double MIN_WILL_FOR_EXECUTE = 1.0;
    private static final double MAX_WILL_FOR_EXECUTE = 4096.0;
    private static final double MIN_EXECUTE_PERCENT = 0.01; // 1%
    private static final double MAX_EXECUTE_PERCENT = 0.15; // 15%
    private static final double WILL_COST_PER_EXECUTE = 5.0;
    private static final int LP_REWARD_PER_EXECUTE = 200;
    private static final int LP_COST_PER_ACTION = 5; // LP cost per hit or mine

    // Netherite pickaxe base speed (9.0) + Efficiency V bonus
    // Efficiency formula: base_speed + (level^2 + 1) = 9.0 + (5^2 + 1) = 9.0 + 26 = 35.0
    private static final float MONK_MINING_SPEED = 35.0f;

    /**
     * Check if a player has an active Sigil of the Monk.
     * The sigil must be in the player's inventory and activated.
     */
    public static boolean hasActiveMonkSigil(Player player) {
        // Check main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_MONK.get())) {
                if (stack.getItem() instanceof com.breakinblocks.neovitae.common.item.IActivatable activatable) {
                    if (activatable.getActivated(stack)) {
                        return true;
                    }
                }
            }
        }
        // Check offhand
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_MONK.get())) {
                if (stack.getItem() instanceof com.breakinblocks.neovitae.common.item.IActivatable activatable) {
                    if (activatable.getActivated(stack)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Apply unarmed damage bonus when attacking with empty hands
     * Also applies knockback to the target
     * Adds demon will scaling damage (1-15% of target max health based on will 1-4096)
     * Execute mechanic: if target falls below execute threshold, instant kill + rewards
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        // Check if damage source is from a player
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check if player is attacking with empty hands
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!hasActiveMonkSigil(player)) {
            return;
        }

        // Get unarmed damage attribute value
        double unarmedDamage = player.getAttributeValue(AnimusAttributes.UNARMED_DAMAGE);
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
            executePercent = MIN_EXECUTE_PERCENT + willRatio * (MAX_EXECUTE_PERCENT - MIN_EXECUTE_PERCENT);

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
        MonkSigilEffect.consumeLP(player, LP_COST_PER_ACTION);

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
                        ParticleTypes.SOUL,
                        x, y, z,
                        20, // count
                        0.5, 0.5, 0.5, // spread
                        0.1 // speed
                    );
                }

                // Add 200 LP to player's soul network
                SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
                if (network != null) {
                    network.add(SoulTicket.create(LP_REWARD_PER_EXECUTE), LP_REWARD_PER_EXECUTE);
                }
            }
        }

        // Apply knockback to the target
        double knockbackStrength = 0.5D; // Similar to knockback enchantment level 1
        double dx = player.getX() - target.getX();
        double dz = player.getZ() - target.getZ();
        target.knockback(knockbackStrength, dx, dz);

        // Apply Soul Snare effect (2 seconds, amplifier 1) for guaranteed will drops
        target.addEffect(new MobEffectInstance(BMMobEffects.SOUL_SNARE, 40, 1));
    }

    /**
     * Check if an item is a catchable projectile item (arrow, spectral arrow, trident, firework)
     */
    private static boolean isCatchableProjectileItem(ItemStack stack) {
        return stack.is(Items.ARROW) ||
               stack.is(Items.SPECTRAL_ARROW) ||
               stack.is(Items.TRIDENT) ||
               stack.is(Items.FIREWORK_ROCKET);
    }

    /**
     * Catch projectiles when hit with empty main hand or holding a catchable projectile
     * Cancels damage, gives the projectile item to the player, plays sound and particles
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerHurtByProjectile(LivingIncomingDamageEvent event) {
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
        if (!hasActiveMonkSigil(player)) {
            return;
        }

        // Check if damage is from a projectile
        if (!(event.getSource().getDirectEntity() instanceof Projectile projectile)) {
            return;
        }

        // Determine what item to give the player based on projectile type
        ItemStack caughtItem = ItemStack.EMPTY;

        if (projectile instanceof Arrow) {
            // Check if it's a spectral arrow or regular arrow
            if (projectile instanceof SpectralArrow) {
                caughtItem = new ItemStack(Items.SPECTRAL_ARROW);
            } else {
                caughtItem = new ItemStack(Items.ARROW);
            }
        } else if (projectile instanceof ThrownTrident) {
            caughtItem = new ItemStack(Items.TRIDENT);
        } else if (projectile instanceof FireworkRocketEntity) {
            caughtItem = new ItemStack(Items.FIREWORK_ROCKET);
        }

        // If we identified a catchable projectile
        if (!caughtItem.isEmpty()) {
            // Cancel the damage
            event.setCanceled(true);

            // Add caught item to player's inventory (or increment stack if holding same type)
            if (mainHandItem.isEmpty()) {
                // Empty hand - put item directly in main hand
                player.setItemInHand(InteractionHand.MAIN_HAND, caughtItem);
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
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                1.0f,
                0.8f + player.getRandom().nextFloat() * 0.4f
            );

            // Spawn breeze particles around the player
            if (player.level() instanceof ServerLevel serverLevel) {
                // Use white poof/cloud particles as breeze-like effect
                serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    player.getX(),
                    player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(),
                    15, // count
                    0.4, 0.4, 0.4, // spread
                    0.05 // speed
                );
                // Add some white particles for extra flair
                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    player.getX(),
                    player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(),
                    10, // count
                    0.3, 0.3, 0.3, // spread
                    0.02 // speed
                );
            }

            // Consume LP for the catch action (50 LP)
            MonkSigilEffect.consumeLP(player, 50);
        }
    }

    /**
     * Cancel fall damage for 25 LP
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFallDamage(LivingIncomingDamageEvent event) {
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
        if (!hasActiveMonkSigil(player)) {
            return;
        }

        // Cancel fall damage and consume 25 LP
        event.setCanceled(true);
        MonkSigilEffect.consumeLP(player, 25);

        // Spawn some landing particles
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.CLOUD,
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
     * Grant knockback resistance and regeneration on kill
     * When killing an enemy with empty hands while sigil is active:
     * - Grants Knockback Resistance II for 5 seconds
     * - Grants Regeneration I for 2 seconds
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Check if killer is a player
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check if player killed with empty hands
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!hasActiveMonkSigil(player)) {
            return;
        }

        // Grant Resistance II for 5 seconds (100 ticks) - reduces damage taken
        player.addEffect(new MobEffectInstance(
            MobEffects.DAMAGE_RESISTANCE,
            100, // 5 seconds
            1    // Level 2 (0-indexed)
        ));

        // Grant Regeneration I for 2 seconds (40 ticks)
        player.addEffect(new MobEffectInstance(
            MobEffects.REGENERATION,
            40,  // 2 seconds
            0    // Level 1 (0-indexed)
        ));
    }

    /**
     * Grant netherite + efficiency 5 mining speed with empty hands
     */
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();

        // Check if player is mining with empty hands
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        // Check if player has an active Sigil of the Monk
        if (!hasActiveMonkSigil(player)) {
            return;
        }

        // Grant netherite + efficiency 5 mining speed for all blocks
        event.setNewSpeed(MONK_MINING_SPEED);
    }

    /**
     * Consume LP when breaking a block with empty hands
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

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
        if (!hasActiveMonkSigil(player)) {
            return;
        }

        // Consume LP per block broken
        MonkSigilEffect.consumeLP(player, LP_COST_PER_ACTION);
    }

    /**
     * Get the total demon will across all types for a player
     */
    private static double getTotalDemonWill(Player player) {
        double total = 0;
        for (EnumWillType type : EnumWillType.values()) {
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
        EnumWillType[] preferredOrder = {
            EnumWillType.DEFAULT,
            EnumWillType.VENGEFUL,
            EnumWillType.STEADFAST,
            EnumWillType.CORROSIVE,
            EnumWillType.DESTRUCTIVE
        };

        for (EnumWillType type : preferredOrder) {
            if (remaining <= 0) break;

            double available = PlayerDemonWillHandler.getTotalDemonWill(type, player);
            if (available > 0) {
                double toConsume = Math.min(remaining, available);
                PlayerDemonWillHandler.consumeDemonWill(type, player, toConsume);
                remaining -= toConsume;
            }
        }
    }
}
