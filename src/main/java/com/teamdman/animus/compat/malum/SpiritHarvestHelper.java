package com.teamdman.animus.compat.malum;

import com.teamdman.animus.Animus;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Helper class for integrating with Malum's spirit harvesting system
 * Uses reflection to avoid hard dependencies on Malum classes
 */
public class SpiritHarvestHelper {

    private static boolean apiChecked = false;
    private static java.lang.reflect.Method spawnSpiritsMethod = null;

    /**
     * Trigger Malum's spirit harvesting when an entity is killed
     * This will spawn soul particles and collect spirits if Malum is present
     *
     * @param target The entity that was killed
     * @param attacker The player who killed the entity
     * @param weapon The weapon used to kill (usually a scythe)
     */
    public static void harvestSpirits(LivingEntity target, Player attacker, ItemStack weapon) {
        // Cache the reflection lookup
        if (!apiChecked) {
            apiChecked = true;
            try {
                // Try Malum 1.21.1 API path
                Class<?> spiritHarvestHandler = Class.forName("com.sammy.malum.core.handlers.SpiritHarvestHandler");
                spawnSpiritsMethod = spiritHarvestHandler.getMethod(
                    "spawnSpirits",
                    LivingEntity.class,
                    LivingEntity.class,
                    ItemStack.class
                );
                Animus.LOGGER.info("Malum spirit harvest API found - integration active");
            } catch (ClassNotFoundException e) {
                Animus.LOGGER.debug("Malum SpiritHarvestHandler not found - spirit integration disabled");
            } catch (NoSuchMethodException e) {
                Animus.LOGGER.warn("Malum API changed - spawnSpirits method not found: {}", e.getMessage());
            } catch (Exception e) {
                Animus.LOGGER.warn("Failed to initialize Malum spirit harvest integration: {}", e.getMessage());
            }
        }

        // If we found the API, use it
        if (spawnSpiritsMethod != null) {
            try {
                spawnSpiritsMethod.invoke(null, target, attacker, weapon);
                Animus.LOGGER.debug("Triggered Malum spirit harvest for {}", target.getType().getDescriptionId());
            } catch (Exception e) {
                Animus.LOGGER.debug("Failed to trigger Malum spirit harvest: {}", e.getMessage());
            }
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
