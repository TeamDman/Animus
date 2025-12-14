package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.LivingUpgradeHelper;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import wayoftime.bloodmagic.common.living.LivingUpgrade;
import wayoftime.bloodmagic.common.registry.BMRegistries;

/**
 * Arcane Channeling - Living Armor upgrade tree for Iron's Spells casters
 *
 * Level 1: 5% mana cost reduction
 * Level 2: 10% mana cost reduction (total)
 * Level 3: 5% cooldown reduction
 * Level 4: 10% cooldown reduction (total)
 * Level 5: Casting grants brief damage resistance (Resistance I for 2 seconds)
 */
public class ArcaneChannelingHandler {

    public static final ResourceKey<LivingUpgrade> UPGRADE_KEY = ResourceKey.create(
        BMRegistries.Keys.LIVING_UPGRADES,
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "arcane_channeling")
    );

    // ResourceLocation for our cooldown reduction attribute modifier (replaces UUID in 1.21)
    private static final ResourceLocation COOLDOWN_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "arcane_channeling_cooldown");

    public static void register() {
        NeoForge.EVENT_BUS.register(new ArcaneChannelingHandler());
    }

    /**
     * Calculate mana cost reduction percentage based on upgrade level
     * Level 1: 5%, Level 2: 10%
     */
    private double getManaCostReduction(int upgradeLevel) {
        if (upgradeLevel >= 2) {
            return 0.10; // 10% reduction
        } else if (upgradeLevel >= 1) {
            return 0.05; // 5% reduction
        }
        return 0.0;
    }

    /**
     * Calculate cooldown reduction percentage based on upgrade level
     * Level 3: 5%, Level 4: 10%
     */
    private double getCooldownReduction(int upgradeLevel) {
        if (upgradeLevel >= 4) {
            return 0.10; // 10% reduction
        } else if (upgradeLevel >= 3) {
            return 0.05; // 5% reduction
        }
        return 0.0;
    }

    /**
     * Apply mana cost reduction when a spell is cast
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSpellCast(SpellOnCastEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        int upgradeLevel = LivingUpgradeHelper.getUpgradeLevel(player, UPGRADE_KEY);
        if (upgradeLevel <= 0) {
            return;
        }

        // Levels 1-2: Reduce mana cost
        double reduction = getManaCostReduction(upgradeLevel);
        if (reduction > 0) {
            int originalCost = event.getManaCost();
            int reducedCost = (int) Math.max(1, originalCost * (1.0 - reduction));
            event.setManaCost(reducedCost);
        }
    }

    /**
     * Grant damage resistance when starting to cast a spell (Level 5)
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onSpellPreCast(SpellPreCastEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        int upgradeLevel = LivingUpgradeHelper.getUpgradeLevel(player, UPGRADE_KEY);

        // Level 5: Grant damage resistance when casting
        if (upgradeLevel >= 5) {
            player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                40, // 2 seconds
                0,  // Level I
                false,
                false,
                true
            ));
        }
    }

    /**
     * Manage cooldown reduction attribute modifier based on upgrade level
     * Runs every second to check and update the modifier as needed
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only check every 20 ticks (1 second) to reduce overhead
        if (player.tickCount % 20 != 0) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        int upgradeLevel = LivingUpgradeHelper.getUpgradeLevel(player, UPGRADE_KEY);
        double targetReduction = getCooldownReduction(upgradeLevel);

        // Get the cooldown reduction attribute
        Holder<net.minecraft.world.entity.ai.attributes.Attribute> cooldownAttr = AttributeRegistry.COOLDOWN_REDUCTION;
        AttributeInstance cooldownAttribute = player.getAttribute(cooldownAttr);
        if (cooldownAttribute == null) {
            return;
        }

        // Check if we already have a modifier
        AttributeModifier existingModifier = cooldownAttribute.getModifier(COOLDOWN_MODIFIER_ID);

        if (targetReduction <= 0) {
            // Remove modifier if we shouldn't have one
            if (existingModifier != null) {
                cooldownAttribute.removeModifier(COOLDOWN_MODIFIER_ID);
            }
        } else {
            // Add or update modifier
            if (existingModifier == null || Math.abs(existingModifier.amount() - targetReduction) > 0.001) {
                // Remove old modifier if it exists with wrong value
                if (existingModifier != null) {
                    cooldownAttribute.removeModifier(COOLDOWN_MODIFIER_ID);
                }
                // Add new modifier
                cooldownAttribute.addPermanentModifier(new AttributeModifier(
                    COOLDOWN_MODIFIER_ID,
                    targetReduction,
                    AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }
    }
}
