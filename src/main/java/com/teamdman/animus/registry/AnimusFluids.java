package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Constants.Mod.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(ForgeRegistries.FLUIDS, Constants.Mod.MODID);

    // Fluid types and fluids will be registered here
    // Example pattern:
    // public static final RegistryObject<FluidType> CUSTOM_FLUID_TYPE = FLUID_TYPES.register("custom",
    //     () -> new FluidType(FluidType.Properties.create()...));
    // public static final RegistryObject<FlowingFluid> CUSTOM_FLOWING = FLUIDS.register("custom_flowing", ...);
    // public static final RegistryObject<FlowingFluid> CUSTOM_STILL = FLUIDS.register("custom_still", ...);

    // Placeholder for fluids to be ported
    // Antimatter fluid
    // Dirt fluid
}
