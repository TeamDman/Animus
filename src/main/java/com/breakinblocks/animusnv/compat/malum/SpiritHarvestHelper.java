package com.breakinblocks.animusnv.compat.malum;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Helper class for integrating with Malum's spirit harvesting system
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
        Holder.Reference<Attribute> proficiency = BuiltInRegistries.ATTRIBUTE.getHolder(SCYTHE_PROFICIENCY).orElse(null);
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
        var curiosOpt = CuriosApi.getCuriosInventory(entity);
        if (curiosOpt.isEmpty()) {
            return false;
        }

        var handler = curiosOpt.get().getEquippedCurios();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack equipped = handler.getStackInSlot(slot);
            if (!equipped.isEmpty() && itemId.equals(BuiltInRegistries.ITEM.getKey(equipped.getItem()))) {
                return true;
            }
        }

        return false;
    }

}
