package com.breakinblocks.animusnv.mixin.evilcraft;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.evilcraft.core.recipe.type.RecipeBloodExtractorCombination;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin for RecipeBloodExtractorCombination to accept Essentia Vitae in
 * blood extractor combination recipes and produce Essentia Vitae output.
 *
 * Redirects getFluid() checks so that essentia_vitae passes the blood validation,
 * and redirects the output FluidStack creation to use essentia_vitae.
 */
@Mixin(RecipeBloodExtractorCombination.class)
public abstract class RecipeBloodExtractorCombinationMixin {

    /**
     * Redirect getFluid() in matches() - makes essentia_vitae pass the blood check.
     */
    @Redirect(method = "matches",
        at = @At(value = "INVOKE",
                 target = "Lnet/neoforged/neoforge/fluids/FluidStack;getFluid()Lnet/minecraft/world/level/material/Fluid;"),
        require = 0)
    private Fluid animus$redirectMatchesFluidCheck(FluidStack instance) {
        Fluid fluid = instance.getFluid();
        if (EvilCraftBloodHelper.isEssentiaVitae(fluid)) {
            return org.cyclops.evilcraft.RegistryEntries.FLUID_BLOOD.get();
        }
        return fluid;
    }

    /**
     * Redirect getFluid() in assemble() - makes essentia_vitae pass the blood check
     * and output essentia_vitae.
     */
    @Redirect(method = "assemble",
        at = @At(value = "INVOKE",
                 target = "Lnet/neoforged/neoforge/fluids/FluidStack;getFluid()Lnet/minecraft/world/level/material/Fluid;"),
        require = 0)
    private Fluid animus$redirectAssembleFluidCheck(FluidStack instance) {
        Fluid fluid = instance.getFluid();
        if (EvilCraftBloodHelper.isEssentiaVitae(fluid)) {
            return org.cyclops.evilcraft.RegistryEntries.FLUID_BLOOD.get();
        }
        return fluid;
    }
}
