package com.breakinblocks.animusnv.mixin.evilcraft;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.neovitae.common.fluid.NVFluids;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.cyclops.evilcraft.RegistryEntries;

/**
 * Shared helper for EvilCraft blood unification mixins.
 * When unification is enabled, all EvilCraft blood references are redirected
 * to NeoVitae's Essentia Vitae.
 */
public final class EvilCraftBloodHelper {

    private EvilCraftBloodHelper() {}

    /**
     * Returns true if blood unification is enabled in the Animus config.
     */
    public static boolean isUnificationEnabled() {
        try {
            return AnimusConfig.evilcraft.enableBloodUnification.get();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the effective blood fluid - essentia_vitae when unification is enabled,
     * EvilCraft blood otherwise.
     */
    public static Fluid getEffectiveBloodFluid() {
        if (isUnificationEnabled()) {
            return NVFluids.ESSENTIA_VITAE_SOURCE.get();
        }
        return RegistryEntries.FLUID_BLOOD.get();
    }

    /**
     * Returns the effective blood holder for use in FluidStack constructors.
     */
    @SuppressWarnings("unchecked")
    public static DeferredHolder<Fluid, ?> getEffectiveBloodHolder() {
        if (isUnificationEnabled()) {
            return (DeferredHolder<Fluid, ?>) (DeferredHolder<?, ?>) NVFluids.ESSENTIA_VITAE_SOURCE;
        }
        return RegistryEntries.FLUID_BLOOD;
    }

    /**
     * Returns true if the given fluid is NeoVitae's Essentia Vitae source fluid
     * and unification is enabled.
     */
    public static boolean isEssentiaVitae(Fluid fluid) {
        return isUnificationEnabled() && fluid == NVFluids.ESSENTIA_VITAE_SOURCE.get();
    }

    /**
     * Returns true if the given fluid is EvilCraft's blood fluid.
     */
    public static boolean isEvilCraftBlood(Fluid fluid) {
        try {
            return fluid == RegistryEntries.FLUID_BLOOD.get();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns true if the given fluid should be treated as "blood" for EvilCraft purposes.
     * Accepts both EvilCraft blood and essentia_vitae (when unification is enabled).
     */
    public static boolean isBloodOrUnified(Fluid fluid) {
        if (isEvilCraftBlood(fluid)) return true;
        return isEssentiaVitae(fluid);
    }
}
