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
import com.breakinblocks.neovitae.common.item.DaggerOfSacrificeItem;

/**
 * Mixin for NeoVitae's Dagger of Sacrifice to apply the bonus_sacrifice attribute.
 * Multiplies the LP amount passed to sacrificialDaggerCall by (1 + bonus_sacrifice/100).
 */
@Mixin(DaggerOfSacrificeItem.class)
public class DaggerOfSacrificeMixin {

    @Unique
    private static final ThreadLocal<LivingEntity> animus$currentAttacker = new ThreadLocal<>();

    /**
     * Capture the attacker at the start of hurtEnemy so we can read their attribute later.
     */
    @Inject(method = "hurtEnemy", at = @At("HEAD"))
    private void animus$captureAttacker(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        animus$currentAttacker.set(attacker);
    }

    /**
     * Modify the LP amount (1st argument, index 0) passed to BloodAltarTile.sacrificialDaggerCall()
     * to apply the bonus sacrifice attribute multiplier.
     */
    @ModifyArg(
        method = "hurtEnemy",
        at = @At(value = "INVOKE", target = "Lcom/breakinblocks/neovitae/common/blockentity/BloodAltarTile;sacrificialDaggerCall(IZ)V"),
        index = 0
    )
    private int animus$applyBonusSacrifice(int originalAmount) {
        LivingEntity attacker = animus$currentAttacker.get();
        animus$currentAttacker.remove();

        if (attacker instanceof Player player) {
            double bonusPercent = player.getAttributeValue(AnimusAttributes.BONUS_SACRIFICE);
            if (bonusPercent > 0) {
                return (int) (originalAmount * (1 + bonusPercent / 100.0));
            }
        }

        return originalAmount;
    }
}
