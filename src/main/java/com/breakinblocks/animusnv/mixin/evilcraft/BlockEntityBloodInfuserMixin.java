package com.breakinblocks.animusnv.mixin.evilcraft;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.cyclops.evilcraft.blockentity.BlockEntityBloodInfuser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin for BlockEntityBloodInfuser to use Essentia Vitae in recipe matching.
 * The infuser creates blood FluidStacks for recipe lookups - redirect these
 * to essentia_vitae so recipes match correctly when the tank contains essentia_vitae.
 */
@Mixin(BlockEntityBloodInfuser.class)
public abstract class BlockEntityBloodInfuserMixin {

    /**
     * Redirect FluidStack.getFluid() in the recipe cache lookup methods.
     * When essentia_vitae is in the tank, the recipe matching FluidStack
     * needs to match what's actually in the tank.
     */
    @Redirect(method = "lambda$new$1",
        at = @At(value = "INVOKE",
                 target = "Lnet/neoforged/neoforge/fluids/FluidStack;getFluid()Lnet/minecraft/world/level/material/Fluid;"),
        require = 0)
    private Fluid animus$redirectRecipeCacheFluid(FluidStack instance) {
        Fluid fluid = instance.getFluid();
        if (EvilCraftBloodHelper.isEssentiaVitae(fluid)) {
            return org.cyclops.evilcraft.RegistryEntries.FLUID_BLOOD.get();
        }
        return fluid;
    }
}
