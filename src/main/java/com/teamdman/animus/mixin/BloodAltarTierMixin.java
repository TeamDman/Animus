package com.teamdman.animus.mixin;

import com.teamdman.animus.advancements.AnimusCriteriaTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wayoftime.bloodmagic.altar.AltarTier;
import wayoftime.bloodmagic.altar.BloodAltar;
import wayoftime.bloodmagic.common.tile.TileAltar;

/**
 * Mixin for Blood Magic's BloodAltar to report altar tier milestones to advancements.
 *
 * checkTier() runs whenever the structure around an altar is revalidated, so the highest
 * tier reached so far is tracked to avoid re-firing the trigger on every revalidation.
 */
@Mixin(value = BloodAltar.class, remap = false)
public class BloodAltarTierMixin {

    @Shadow
    private TileAltar tileAltar;

    @Shadow
    private AltarTier altarTier;

    @Unique
    private int animus$highestTierReported = 0;

    @Inject(method = "checkTier", at = @At("RETURN"))
    private void animus$reportTier(CallbackInfo ci) {
        if (altarTier == null) {
            return;
        }

        int tier = altarTier.toInt();
        if (tier <= animus$highestTierReported) {
            return;
        }

        animus$highestTierReported = tier;
        AnimusCriteriaTriggers.onAltarTierFormed(tileAltar, tier);
    }
}
