package com.breakinblocks.animusnv.mixin.evilcraft;

import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.cyclopscore.fluid.SingleUseTank;
import org.cyclops.evilcraft.blockentity.BlockEntityPurifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin for BlockEntityPurifier to set tank contents to Essentia Vitae
 * when the bucket level is changed.
 */
@Mixin(BlockEntityPurifier.class)
public abstract class BlockEntityPurifierMixin {

    @Redirect(method = "setBuckets",
        at = @At(value = "INVOKE",
                 target = "Lorg/cyclops/cyclopscore/fluid/SingleUseTank;setFluid(Lnet/neoforged/neoforge/fluids/FluidStack;)V"))
    private void animus$redirectSetFluid(SingleUseTank tank, FluidStack fluidStack) {
        if (EvilCraftBloodHelper.isUnificationEnabled()) {
            fluidStack = new FluidStack(NVFluids.ESSENTIA_VITAE_SOURCE, fluidStack.getAmount());
        }
        tank.setFluid(fluidStack);
    }
}
