package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.Constants;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import wayoftime.bloodmagic.core.living.ILivingContainer;
import wayoftime.bloodmagic.core.living.LivingStats;
import wayoftime.bloodmagic.core.living.LivingUpgrade;

import java.util.UUID;

/**
 * Arcane Channeling - Living Armor upgrade tree for spellcasters
 *
 * Level 1: 5% mana cost reduction
 * Level 2: 10% mana cost reduction (total)
 * Level 3: 5% cooldown reduction
 * Level 4: 10% cooldown reduction (total)
 * Level 5: Casting grants brief damage resistance (Resistance I for 2 seconds)
 */
public class UpgradeArcaneChanneling extends LivingUpgrade {

    public static final ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath(
        Constants.Mod.MODID,
        "upgrade.arcane_channeling"
    );

    // UUID for our cooldown reduction attribute modifier
    private static final UUID COOLDOWN_MODIFIER_UUID = UUID.fromString("a8e7f5c3-9d4b-4e2a-b1c6-3f8d9e0a2b5c");
    private static final String COOLDOWN_MODIFIER_NAME = "Arcane Channeling Cooldown Reduction";

    public UpgradeArcaneChanneling() {
        super(KEY, levels -> {
            // Define XP thresholds for each level
            levels.add(new Level(0, 5));    // Level 1: 5 upgrade points
            levels.add(new Level(0, 10));   // Level 2: 10 points
            levels.add(new Level(0, 15));   // Level 3: 15 points
            levels.add(new Level(0, 20));   // Level 4: 20 points
            levels.add(new Level(0, 25));   // Level 5: 25 points
        });
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Get the upgrade level for this player
     * Checks all armor pieces and returns the highest level found
     */
    private int getUpgradeLevel(Player player) {
        int maxLevel = 0;
        for (ItemStack armorPiece : player.getInventory().armor) {
            if (armorPiece.getItem() instanceof ILivingContainer container) {
                LivingStats stats = container.getLivingStats(armorPiece);
                if (stats != null) {
                    int level = stats.getLevel(KEY);
                    maxLevel = Math.max(maxLevel, level);
                }
            }
        }
        return maxLevel;
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
     * Uses SpellOnCastEvent which allows modification of mana cost
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSpellCast(SpellOnCastEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        int upgradeLevel = getUpgradeLevel(player);
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

        int upgradeLevel = getUpgradeLevel(player);

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
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;

        // Only check every 20 ticks (1 second) to reduce overhead
        if (player.tickCount % 20 != 0) {
            return;
        }

        // Only run on server side
        if (player.level().isClientSide()) {
            return;
        }

        int upgradeLevel = getUpgradeLevel(player);
        double targetReduction = getCooldownReduction(upgradeLevel);

        // Get the cooldown reduction attribute
        AttributeInstance cooldownAttribute = player.getAttribute(AttributeRegistry.COOLDOWN_REDUCTION.get());
        if (cooldownAttribute == null) {
            return;
        }

        // Check if we already have a modifier
        AttributeModifier existingModifier = cooldownAttribute.getModifier(COOLDOWN_MODIFIER_UUID);

        if (targetReduction <= 0) {
            // Remove modifier if we shouldn't have one
            if (existingModifier != null) {
                cooldownAttribute.removeModifier(COOLDOWN_MODIFIER_UUID);
            }
        } else {
            // Add or update modifier
            // Iron's Spellbooks cooldown reduction is a percentage, where 0.05 = 5% reduction
            if (existingModifier == null || Math.abs(existingModifier.getAmount() - targetReduction) > 0.001) {
                // Remove old modifier if it exists with wrong value
                if (existingModifier != null) {
                    cooldownAttribute.removeModifier(COOLDOWN_MODIFIER_UUID);
                }
                // Add new modifier
                cooldownAttribute.addPermanentModifier(new AttributeModifier(
                    COOLDOWN_MODIFIER_UUID,
                    COOLDOWN_MODIFIER_NAME,
                    targetReduction,
                    AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }
}
