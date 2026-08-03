package com.breakinblocks.animusnv.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Malum's scythe necklaces, looked up purely by id so this stays compilable while the
 * Malum compat module is switched off for 26.1.
 */
public final class MalumScytheCurios {

    private static final Identifier NARROW_EDGE_NECKLACE = Identifier.fromNamespaceAndPath("malum", "necklace_of_the_narrow_edge");
    private static final Identifier HIDDEN_BLADE_NECKLACE = Identifier.fromNamespaceAndPath("malum", "necklace_of_the_hidden_blade");
    private static final Identifier SCYTHE_PROFICIENCY = Identifier.fromNamespaceAndPath("malum", "scythe_proficiency");

    private MalumScytheCurios() {
    }

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
        Holder.Reference<Attribute> proficiency = BuiltInRegistries.ATTRIBUTE.get(SCYTHE_PROFICIENCY).orElse(null);
        if (proficiency == null) {
            return 1.0;
        }

        AttributeInstance instance = attacker.getAttribute(proficiency);
        if (instance == null) {
            return 1.0;
        }

        return instance.getValue();
    }

    private static boolean hasCurioEquipped(LivingEntity entity, Identifier itemId) {
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
