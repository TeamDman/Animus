package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.neovitae.common.fluid.NVFluids;
import org.cyclops.evilcraft.RegistryEntries;
import org.cyclops.evilcraft.core.fluid.BloodFluidConverter;

/**
 * Compatibility module for EvilCraft blood unification.
 *
 * When enabled via config, this module:
 * 1. Changes BloodFluidConverter's target from evilcraft:blood to essentia_vitae
 * 2. Registers evilcraft:blood as a convertible fluid (so existing blood still works)
 *
 * Combined with the EvilCraft mixins, this makes ALL of EvilCraft's blood system
 * use NeoVitae's Essentia Vitae instead.
 */
public class EvilCraftCompat implements ICompatModule {

    @Override
    public void init() {
        if (!AnimusConfig.evilcraft.enableBloodUnification.get()) {
            Animus.LOGGER.debug("EvilCraft blood unification is disabled in config");
            return;
        }

        double ratio = AnimusConfig.evilcraft.conversionRatio.get();

        // Change the converter's target fluid from blood to essentia_vitae
        BloodFluidConverter converter = BloodFluidConverter.getInstance();
        converter.setTarget(NVFluids.ESSENTIA_VITAE_SOURCE.get());

        // Register original blood as convertible so existing blood in the world still works
        converter.addConverter(RegistryEntries.FLUID_BLOOD.get(), ratio);

        Animus.LOGGER.info("EvilCraft blood unification enabled: all blood is now Essentia Vitae (ratio: {})", ratio);
    }

    @Override
    public String getModId() {
        return "evilcraft";
    }
}
