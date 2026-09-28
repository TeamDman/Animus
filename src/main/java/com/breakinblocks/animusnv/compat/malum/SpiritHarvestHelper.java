package com.breakinblocks.animusnv.compat.malum;

import com.sammy.malum.core.handlers.enchantment.AscensionHandler;
import com.sammy.malum.core.handlers.enchantment.ReboundHandler;
import com.sammy.malum.registry.common.MalumDamageTypes;
import com.sammy.malum.registry.common.enchantment.EnchantmentKeys;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import team.lodestar.lodestone.helpers.DamageTypeHelper;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Helper class for integrating with Malum's spirit harvesting system
 */
public class SpiritHarvestHelper {

    private static final ResourceLocation NARROW_EDGE_NECKLACE = ResourceLocation.fromNamespaceAndPath("malum", "necklace_of_the_narrow_edge");
    private static final ResourceLocation HIDDEN_BLADE_NECKLACE = ResourceLocation.fromNamespaceAndPath("malum", "necklace_of_the_hidden_blade");

    /**
     * Mirrors MalumScytheItem.canSweep: Malum suppresses scythe sweeping while either the
     * Necklace of the Narrow Edge or the Necklace of the Hidden Blade is worn.
     */
    public static boolean isSweepSuppressed(LivingEntity attacker) {
        return hasCurioEquipped(attacker, NARROW_EDGE_NECKLACE) || hasCurioEquipped(attacker, HIDDEN_BLADE_NECKLACE);
    }

    public static DamageSource scytheMeleeSource(Player player) {
        return DamageTypeHelper.create(player.level(), MalumDamageTypes.SCYTHE_MELEE, player);
    }

    public static DamageSource scytheSweepSource(Player player) {
        return DamageTypeHelper.create(player.level(), MalumDamageTypes.SCYTHE_SWEEP, player);
    }

    public static boolean tryScytheAbility(Level level, Player player, InteractionHand hand, ItemStack stack) {
        if (EnchantmentKeys.getEnchantmentLevel(level, EnchantmentKeys.REBOUND, stack) > 0) {
            ReboundHandler.throwScythe(level, player, hand, stack);
            return true;
        }

        if (EnchantmentKeys.getEnchantmentLevel(level, EnchantmentKeys.ASCENSION, stack) > 0) {
            AscensionHandler.triggerAscension(level, player, hand, stack);
            return true;
        }

        return false;
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
