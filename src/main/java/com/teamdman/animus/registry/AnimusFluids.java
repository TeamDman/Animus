package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Constants.Mod.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(ForgeRegistries.FLUIDS, Constants.Mod.MODID);

    // Antimatter Fluid Type
    public static final RegistryObject<FluidType> ANTIMATTER_FLUID_TYPE = FLUID_TYPES.register(
        "antimatter",
        () -> new FluidType(FluidType.Properties.create()
            .density(10000)
            .viscosity(1)
            .temperature(0)
            .canSwim(false)
            .canDrown(true)
            .pathType(null)
            .adjacentPathType(null)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
        ) {
            @Override
            public String getDescriptionId() {
                return "fluid.animus.antimatter";
            }
        }
    );

    // Dirt Fluid Type
    public static final RegistryObject<FluidType> DIRT_FLUID_TYPE = FLUID_TYPES.register(
        "dirt",
        () -> new FluidType(FluidType.Properties.create()
            .density(750)
            .viscosity(200)
            .temperature(200)
            .canSwim(false)
            .canDrown(true)
            .pathType(null)
            .adjacentPathType(null)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
        ) {
            @Override
            public String getDescriptionId() {
                return "fluid.animus.dirt";
            }
        }
    );

    // Antimatter Fluids
    public static final RegistryObject<FlowingFluid> ANTIMATTER_FLOWING = FLUIDS.register(
        "antimatter_flowing",
        () -> new ForgeFlowingFluid.Flowing(AnimusFluids.ANTIMATTER_PROPERTIES)
    );

    public static final RegistryObject<FlowingFluid> ANTIMATTER_SOURCE = FLUIDS.register(
        "antimatter",
        () -> new ForgeFlowingFluid.Source(AnimusFluids.ANTIMATTER_PROPERTIES)
    );

    // Dirt Fluids
    public static final RegistryObject<FlowingFluid> DIRT_FLOWING = FLUIDS.register(
        "dirt_flowing",
        () -> new ForgeFlowingFluid.Flowing(AnimusFluids.DIRT_PROPERTIES)
    );

    public static final RegistryObject<FlowingFluid> DIRT_SOURCE = FLUIDS.register(
        "dirt",
        () -> new ForgeFlowingFluid.Source(AnimusFluids.DIRT_PROPERTIES)
    );

    // Fluid Properties
    public static final ForgeFlowingFluid.Properties ANTIMATTER_PROPERTIES = new ForgeFlowingFluid.Properties(
        ANTIMATTER_FLUID_TYPE,
        ANTIMATTER_SOURCE,
        ANTIMATTER_FLOWING
    )
        .block(() -> (net.minecraft.world.level.block.LiquidBlock) AnimusBlocks.BLOCK_FLUID_ANTIMATTER.get())
        .bucket(() -> AnimusItems.ANTIMATTER_BUCKET.get());

    public static final ForgeFlowingFluid.Properties DIRT_PROPERTIES = new ForgeFlowingFluid.Properties(
        DIRT_FLUID_TYPE,
        DIRT_SOURCE,
        DIRT_FLOWING
    )
        .block(() -> (net.minecraft.world.level.block.LiquidBlock) AnimusBlocks.BLOCK_FLUID_DIRT.get())
        .bucket(() -> AnimusItems.DIRT_BUCKET.get());
}
