package com.breakinblocks.animusnv.mixin.ironsartifice;

import com.breakinblocks.animusnv.compat.ironsartifice.BloodBulletReload;
import io.redspace.irons_artifice.data.ReloadResult;
import io.redspace.irons_artifice.item.GunplayManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GunplayManager.class, remap = false)
public abstract class GunplayManagerMixin {

    @Inject(method = "attemptStartReload", at = @At("HEAD"), cancellable = true)
    private static void animus$startBloodReload(
            LivingEntity living, ItemStack gun, CallbackInfoReturnable<ReloadResult> cir) {
        ReloadResult result = BloodBulletReload.start(living, gun);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "attemptFinishReload", at = @At("HEAD"), cancellable = true)
    private static void animus$finishBloodReload(
            LivingEntity living, ItemStack gun, int roundsToLoad, CallbackInfoReturnable<ReloadResult> cir) {
        ReloadResult result = BloodBulletReload.finish(living, gun, roundsToLoad);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }
}
