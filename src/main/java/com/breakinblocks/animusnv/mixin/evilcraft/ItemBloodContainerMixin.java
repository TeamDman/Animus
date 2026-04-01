package com.breakinblocks.animusnv.mixin.evilcraft;

import net.minecraft.world.level.material.Fluid;
import org.cyclops.evilcraft.core.item.ItemBloodContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for ItemBloodContainer to redirect the fluid type from blood to
 * Essentia Vitae. This affects all items extending ItemBloodContainer:
 * Blood Extractors, Creative Blood Drops, etc.
 *
 * The getFluid() method is inherited from CyclopsCore's DamageIndicatedItemFluidContainer.
 */
@Mixin(ItemBloodContainer.class)
public abstract class ItemBloodContainerMixin {

    @Inject(method = "getFluid", at = @At("RETURN"), cancellable = true, remap = false)
    private void animus$redirectGetFluid(CallbackInfoReturnable<Fluid> cir) {
        if (EvilCraftBloodHelper.isEvilCraftBlood(cir.getReturnValue())) {
            cir.setReturnValue(EvilCraftBloodHelper.getEffectiveBloodFluid());
        }
    }
}
