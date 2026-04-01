package com.breakinblocks.animusnv.mixin.evilcraft;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.evilcraft.RegistryEntries;
import org.cyclops.evilcraft.blockentity.BlockEntitySanguinaryEnvironmentalAccumulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin for {@link BlockEntitySanguinaryEnvironmentalAccumulator} to accept
 * NeoVitae's Essentia Vitae in external tank checks.
 *
 * The accumulator checks neighboring tanks for blood via a hardcoded
 * {@code == RegistryEntries.FLUID_BLOOD.get()} comparison. This mixin
 * redirects the {@code getFluid()} call so that Essentia Vitae is treated
 * as blood when unification is enabled.
 */
@Mixin(BlockEntitySanguinaryEnvironmentalAccumulator.class)
public abstract class SanguinaryEnvironmentalAccumulatorMixin {

    @Redirect(
        method = "getVirtualTankChildren",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/fluids/FluidStack;getFluid()Lnet/minecraft/world/level/material/Fluid;"
        )
    )
    private Fluid animus$redirectBloodFluidCheck(FluidStack instance) {
        Fluid fluid = instance.getFluid();
        if (EvilCraftBloodHelper.isEssentiaVitae(fluid)) {
            return RegistryEntries.FLUID_BLOOD.get();
        }
        return fluid;
    }
}
