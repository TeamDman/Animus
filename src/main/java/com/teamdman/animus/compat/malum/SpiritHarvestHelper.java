package com.teamdman.animus.compat.malum;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Helper class for integrating with Malum's scythe systems
 * Uses reflection to avoid hard dependencies on Malum classes
 *
 * Spirit harvesting is handled natively by Malum's event system via the
 * malum:scythe item tag (which transitively includes malum:soul_hunter_weapon).
 */
public class SpiritHarvestHelper {

    private static final ResourceLocation NARROW_EDGE_NECKLACE = ResourceLocation.fromNamespaceAndPath("malum", "necklace_of_the_narrow_edge");
    private static final ResourceLocation HIDDEN_BLADE_NECKLACE = ResourceLocation.fromNamespaceAndPath("malum", "necklace_of_the_hidden_blade");
    private static final ResourceLocation SCYTHE_PROFICIENCY = ResourceLocation.fromNamespaceAndPath("malum", "scythe_proficiency");

    /**
     * Mirrors MalumScytheItem.canSweep: Malum suppresses scythe sweeping while either the
     * Necklace of the Narrow Edge or the Necklace of the Hidden Blade is worn.
     */
    public static boolean isSweepSuppressed(LivingEntity attacker) {
        return hasCurioEquipped(attacker, NARROW_EDGE_NECKLACE) || hasCurioEquipped(attacker, HIDDEN_BLADE_NECKLACE);
    }

    /**
     * Malum multiplies scythe damage by its scythe_proficiency attribute, but only for damage
     * sources in the malum:is_scythe damage type tag. Our scythes deal ordinary player attack
     * damage, so the multiplier has to be applied by hand.
     *
     * @return the multiplier to apply, or 1.0 when Malum is absent
     */
    public static double getScytheProficiency(LivingEntity attacker) {
        Attribute proficiency = ForgeRegistries.ATTRIBUTES.getValue(SCYTHE_PROFICIENCY);
        if (proficiency == null) {
            return 1.0;
        }

        AttributeInstance instance = attacker.getAttribute(proficiency);
        if (instance == null) {
            return 1.0;
        }

        return instance.getValue();
    }

    private static boolean hasCurioEquipped(LivingEntity entity, ResourceLocation itemId) {
        var curiosOpt = CuriosApi.getCuriosInventory(entity).resolve();
        if (curiosOpt.isEmpty()) {
            return false;
        }

        var handler = curiosOpt.get().getEquippedCurios();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack equipped = handler.getStackInSlot(slot);
            if (!equipped.isEmpty() && itemId.equals(ForgeRegistries.ITEMS.getKey(equipped.getItem()))) {
                return true;
            }
        }

        return false;
    }

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
