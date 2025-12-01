package com.teamdman.animus.compat.malum;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Helper class for integrating with Malum's spirit harvesting system
 * Uses reflection to avoid hard dependencies on Malum classes
 */
public class SpiritHarvestHelper {

    /**
     * Trigger Malum's spirit harvesting when an entity is killed
     * This will spawn soul particles and collect spirits if Malum is present
     *
     * @param target The entity that was killed
     * @param attacker The player who killed the entity
     * @param weapon The weapon used to kill (usually a scythe)
     */
    public static void harvestSpirits(LivingEntity target, Player attacker, ItemStack weapon) {
        try {
            // Call Malum's spirit harvest handler to spawn souls
            // This uses the public static method: SpiritHarvestHandler.spawnSpirits(target, attacker, weapon)
            Class<?> spiritHarvestHandler = Class.forName("com.sammy.malum.core.handlers.SpiritHarvestHandler");
            java.lang.reflect.Method spawnSpirits = spiritHarvestHandler.getMethod(
                "spawnSpirits",
                LivingEntity.class,
                LivingEntity.class,
                ItemStack.class
            );
            spawnSpirits.invoke(null, target, attacker, weapon);
        } catch (Exception e) {
            // Silent failure - Malum integration is optional
            System.err.println("Failed to trigger Malum soul harvest: " + e.getMessage());
        }
    }

    /**
     * Check if an enchantment is a Malum enchantment
     * Malum enchantments include: haunted, rebounding, spirit plunder, etc.
     *
     * @param enchantmentId The enchantment's description ID
     * @return true if this is a Malum enchantment
     */
    public static boolean isMalumEnchantment(String enchantmentId) {
        if (enchantmentId == null) {
            return false;
        }

        // Check if this is a Malum enchantment
        if (enchantmentId.contains("malum")) {
            // Allow common Malum enchantments on scythes
            return enchantmentId.contains("haunted") ||
                   enchantmentId.contains("rebounding") ||
                   enchantmentId.contains("spirit_plunder");
        }

        return false;
    }
}
