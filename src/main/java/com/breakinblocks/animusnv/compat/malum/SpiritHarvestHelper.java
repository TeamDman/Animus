package com.breakinblocks.animusnv.compat.malum;

import com.breakinblocks.animusnv.Animus;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/**
 * Helper class for integrating with Malum's spirit harvesting system
 * Uses reflection to avoid hard dependencies on Malum classes
 */
public class SpiritHarvestHelper {

    private static boolean apiChecked = false;
    private static Method spawnSpiritsMethod = null;

    public static void harvestSpirits(LivingEntity target, Player attacker, ItemStack weapon) {
        if (!apiChecked) {
            apiChecked = true;
            try {
                Class<?> spiritHarvestHandler = Class.forName("com.sammy.malum.core.handlers.SpiritHarvestHandler");
                spawnSpiritsMethod = spiritHarvestHandler.getMethod(
                    "spawnSpirits",
                    LivingEntity.class,
                    LivingEntity.class,
                    ItemStack.class
                );
                Animus.LOGGER.debug("Malum spirit harvest API found - integration active");
            } catch (ClassNotFoundException e) {
                Animus.LOGGER.debug("Malum SpiritHarvestHandler not found - spirit integration disabled");
            } catch (NoSuchMethodException e) {
                Animus.LOGGER.warn("Malum API changed - spawnSpirits method not found: {}", e.getMessage());
            } catch (Exception e) {
                Animus.LOGGER.warn("Failed to initialize Malum spirit harvest integration: {}", e.getMessage());
            }
        }

        if (spawnSpiritsMethod != null) {
            try {
                spawnSpiritsMethod.invoke(null, target, attacker, weapon);
                Animus.LOGGER.debug("Triggered Malum spirit harvest for {}", target.getType().getDescriptionId());
            } catch (Exception e) {
                Animus.LOGGER.debug("Failed to trigger Malum spirit harvest: {}", e.getMessage());
            }
        }
    }

    public static boolean isMalumEnchantment(String enchantmentId) {
        if (enchantmentId == null) {
            return false;
        }

        if (enchantmentId.contains("malum")) {
            return enchantmentId.contains("haunted") ||
                   enchantmentId.contains("rebounding") ||
                   enchantmentId.contains("spirit_plunder");
        }

        return false;
    }
}
