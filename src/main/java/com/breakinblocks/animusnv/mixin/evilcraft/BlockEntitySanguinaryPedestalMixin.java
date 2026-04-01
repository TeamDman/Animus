package com.breakinblocks.animusnv.mixin.evilcraft;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.cyclops.evilcraft.blockentity.BlockEntitySanguinaryPedestal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin for BlockEntitySanguinaryPedestal to accept Essentia Vitae.
 * The pedestal absorbs blood from blood stains and spiked plates -
 * when unification is enabled, it should accept essentia_vitae as well.
 *
 * Redirects the fill() call on the bonus fluid handler to convert the
 * incoming fluid to the effective blood type.
 */
@Mixin(BlockEntitySanguinaryPedestal.class)
public abstract class BlockEntitySanguinaryPedestalMixin {

    /**
     * Redirect FluidStack.getFluid() calls in the pedestal to accept
     * essentia_vitae as blood.
     */
    @Redirect(method = "*",
        at = @At(value = "INVOKE",
                 target = "Lnet/neoforged/neoforge/fluids/FluidStack;getFluid()Lnet/minecraft/world/level/material/Fluid;"),
        require = 0)
    private Fluid animus$redirectFluidCheck(FluidStack instance) {
        Fluid fluid = instance.getFluid();
        if (EvilCraftBloodHelper.isEssentiaVitae(fluid)) {
            return org.cyclops.evilcraft.RegistryEntries.FLUID_BLOOD.get();
        }
        return fluid;
    }
}
