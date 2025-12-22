package com.teamdman.animus.mixin;

import com.teamdman.animus.altar.IMultiBonusRune;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wayoftime.bloodmagic.altar.AltarComponent;
import wayoftime.bloodmagic.altar.AltarTier;
import wayoftime.bloodmagic.altar.AltarUpgrade;
import wayoftime.bloodmagic.altar.AltarUtil;
import wayoftime.bloodmagic.altar.ComponentType;

/**
 * Mixin for Blood Magic's AltarUtil to support Animus multi-bonus runes.
 *
 * This mixin intercepts the getUpgrades() method to check for IMultiBonusRune
 * implementations and apply their custom multi-bonus logic.
 *
 * Multi-bonus runes (like Arcane Rune and Rune of Unleashed Nature) should NOT
 * extend BlockBloodRune. This ensures they bypass the standard single-rune
 * processing in the original method. Instead, they implement IMultiBonusRune
 * and provide all their bonuses through applyMultiBonuses().
 */
@Mixin(value = AltarUtil.class, remap = false)
public class AltarUtilMixin {

    /**
     * Inject at the end of getUpgrades to apply multi-bonus rune logic.
     *
     * The original method only processes blocks that extend BlockBloodRune.
     * Our multi-bonus runes do NOT extend BlockBloodRune, so they are skipped
     * by the original code. This mixin adds processing for IMultiBonusRune blocks.
     */
    @Inject(
        method = "getUpgrades",
        at = @At("RETURN"),
        cancellable = false
    )
    private static void animus$applyMultiBonusRunes(
        Level world,
        BlockPos pos,
        AltarTier currentTier,
        CallbackInfoReturnable<AltarUpgrade> cir
    ) {
        AltarUpgrade upgrades = cir.getReturnValue();

        // Iterate through all altar components to find multi-bonus runes
        for (AltarComponent component : currentTier.getAltarComponents()) {
            // Only check upgrade slots that accept blood runes
            if (!component.isUpgradeSlot() || component.getComponent() != ComponentType.BLOODRUNE) {
                continue;
            }

            BlockPos componentPos = pos.offset(component.getOffset());
            BlockState state = world.getBlockState(componentPos);

            // Check if this block implements IMultiBonusRune
            // Note: These blocks should NOT extend BlockBloodRune, so they weren't
            // processed by the original method. We add all their bonuses here.
            if (state.getBlock() instanceof IMultiBonusRune multiBonusRune) {
                multiBonusRune.applyMultiBonuses(world, componentPos, upgrades);
            }
        }
    }
}
