package com.teamdman.animus.compat.malum;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Helper class for integrating with Malum's scythe systems
 * Uses reflection to avoid hard dependencies on Malum classes
 *
 * Spirit harvesting is handled natively by Malum's event system via the
 * malum:scythe item tag (which transitively includes malum:soul_hunter_weapon).
 */
public class SpiritHarvestHelper {

    // Cached reflection state for Rebound
    private static Boolean reboundAvailable = null;
    private static java.lang.reflect.Method throwScytheMethod = null;

    // Cached reflection state for Ascension
    private static Boolean ascensionAvailable = null;
    private static java.lang.reflect.Method triggerAscensionMethod = null;

    /**
     * Try to trigger Malum's Rebound enchantment throw behavior via reflection.
     * Calls ReboundEnchantment.throwScythe(Level, Player, InteractionHand, ItemStack)
     *
     * @return true if the throw was triggered, false if Rebound is not available or failed
     */
    public static boolean tryThrowScythe(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (reboundAvailable == null) {
            try {
                Class<?> clazz = Class.forName("com.sammy.malum.common.enchantment.scythe.ReboundEnchantment");
                throwScytheMethod = clazz.getMethod("throwScythe", Level.class, Player.class, InteractionHand.class, ItemStack.class);
                reboundAvailable = true;
            } catch (Exception e) {
                reboundAvailable = false;
            }
        }

        if (!reboundAvailable || throwScytheMethod == null) {
            return false;
        }

        try {
            throwScytheMethod.invoke(null, level, player, hand, stack);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Try to trigger Malum's Ascension enchantment behavior via reflection.
     * Calls AscensionEnchantment.triggerAscension(Level, Player, InteractionHand, ItemStack)
     *
     * @return true if ascension was triggered, false if not available or failed
     */
    public static boolean tryTriggerAscension(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (ascensionAvailable == null) {
            try {
                Class<?> clazz = Class.forName("com.sammy.malum.common.enchantment.scythe.AscensionEnchantment");
                triggerAscensionMethod = clazz.getMethod("triggerAscension", Level.class, Player.class, InteractionHand.class, ItemStack.class);
                ascensionAvailable = true;
            } catch (Exception e) {
                ascensionAvailable = false;
            }
        }

        if (!ascensionAvailable || triggerAscensionMethod == null) {
            return false;
        }

        try {
            triggerAscensionMethod.invoke(null, level, player, hand, stack);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the enchantment level of a Malum enchantment by class name via reflection.
     *
     * @param stack The item stack to check
     * @param enchantClassName Fully-qualified Malum enchantment class name
     * @return The enchantment level, or 0 if not present or not available
     */
    public static int getMalumEnchantmentLevel(ItemStack stack, String enchantClassName) {
        try {
            Class<?> clazz = Class.forName(enchantClassName);
            // Get the enchantment instance from the registry
            var registry = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS;
            for (var entry : registry.getEntries()) {
                if (clazz.isInstance(entry.getValue())) {
                    return stack.getEnchantmentLevel(entry.getValue());
                }
            }
        } catch (Exception e) {
            // Malum not loaded or class not found
        }
        return 0;
    }

    /**
     * Check if an enchantment is a Malum scythe enchantment that should be applicable
     *
     * @param enchantmentId The enchantment's description ID
     * @return true if this is a Malum scythe enchantment
     */
    public static boolean isMalumEnchantment(String enchantmentId) {
        if (enchantmentId == null) {
            return false;
        }

        if (enchantmentId.contains("malum")) {
            return enchantmentId.contains("haunted") ||
                   enchantmentId.contains("rebounding") ||
                   enchantmentId.contains("spirit_plunder") ||
                   enchantmentId.contains("ascension") ||
                   enchantmentId.contains("animated");
        }

        return false;
    }
}
