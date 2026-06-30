package com.teamdman.animus.mixin;

import com.teamdman.animus.registry.AnimusAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wayoftime.bloodmagic.common.item.ItemDaggerOfSacrifice;

/**
 * Mixin for Blood Magic's Dagger of Sacrifice to apply the bonus_sacrifice attribute.
 * Multiplies the LP amount passed to findAndFillAltar by (1 + bonus_sacrifice).
 */
@Mixin(value = ItemDaggerOfSacrifice.class, remap = false)
public class DaggerOfSacrificeMixin {

    @Unique
    private static final ThreadLocal<LivingEntity> animus$currentAttacker = new ThreadLocal<>();

    /**
     * Capture the attacker at the start of hurtEnemy so we can read their attribute later.
     */
    @Inject(method = {"hurtEnemy", "m_7579_"}, at = @At("HEAD"))
    private void animus$captureAttacker(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        animus$currentAttacker.set(attacker);
    }

    /**
     * Modify the amount (3rd argument, index 2) passed to PlayerSacrificeHelper.findAndFillAltar()
     * to apply the bonus sacrifice attribute multiplier.
     */
    @ModifyArg(
        method = {"hurtEnemy", "m_7579_"},
        at = @At(value = "INVOKE", target = "Lwayoftime/bloodmagic/util/helper/PlayerSacrificeHelper;findAndFillAltar(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;IZ)Z"),
        index = 2
    )
    private int animus$applyBonusSacrifice(int originalAmount) {
        LivingEntity attacker = animus$currentAttacker.get();
        animus$currentAttacker.remove();

        if (attacker instanceof Player player) {
            double bonusFraction = player.getAttributeValue(AnimusAttributes.BONUS_SACRIFICE.get());
            if (bonusFraction > 0) {
                return (int) (originalAmount * (1 + bonusFraction));
            }
        }

        return originalAmount;
    }
}
