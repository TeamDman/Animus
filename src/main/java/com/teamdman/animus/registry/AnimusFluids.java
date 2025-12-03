package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, Constants.Mod.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(ForgeRegistries.FLUIDS, Constants.Mod.MODID);

    // AntiLife Fluid Type
    public static final RegistryObject<FluidType> ANTILIFE_FLUID_TYPE = FLUID_TYPES.register(
        "antilife",
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
                return "fluid.animus.antilife";
            }

            @Override
            public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    consumer.accept(com.teamdman.animus.client.AntiLifeFluidClientExtension.INSTANCE);
                });
            }
        }
    );

    // Living Terra Fluid Type
    public static final RegistryObject<FluidType> LIVING_TERRA_FLUID_TYPE = FLUID_TYPES.register(
        "living_terra",
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
                return "fluid.animus.living_terra";
            }

            @Override
            public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                    consumer.accept(com.teamdman.animus.client.LivingTerraFluidClientExtension.INSTANCE);
                });
            }
        }
    );

    // AntiLife Fluids
    public static final RegistryObject<FlowingFluid> ANTILIFE_FLOWING = FLUIDS.register(
        "antilife_flowing",
        () -> new ForgeFlowingFluid.Flowing(AnimusFluids.ANTILIFE_PROPERTIES)
    );

    public static final RegistryObject<FlowingFluid> ANTILIFE_SOURCE = FLUIDS.register(
        "antilife",
        () -> new ForgeFlowingFluid.Source(AnimusFluids.ANTILIFE_PROPERTIES)
    );

    // Living Terra Fluids
    public static final RegistryObject<FlowingFluid> LIVING_TERRA_FLOWING = FLUIDS.register(
        "living_terra_flowing",
        () -> new ForgeFlowingFluid.Flowing(AnimusFluids.LIVING_TERRA_PROPERTIES)
    );

    public static final RegistryObject<FlowingFluid> LIVING_TERRA_SOURCE = FLUIDS.register(
        "living_terra",
        () -> new ForgeFlowingFluid.Source(AnimusFluids.LIVING_TERRA_PROPERTIES)
    );

    // Fluid Properties
    public static final ForgeFlowingFluid.Properties ANTILIFE_PROPERTIES = new ForgeFlowingFluid.Properties(
        ANTILIFE_FLUID_TYPE,
        ANTILIFE_SOURCE,
        ANTILIFE_FLOWING
    )
        .block(() -> (net.minecraft.world.level.block.LiquidBlock) AnimusBlocks.BLOCK_FLUID_ANTILIFE.get())
        .bucket(() -> AnimusItems.ANTILIFE_BUCKET.get());

    public static final ForgeFlowingFluid.Properties LIVING_TERRA_PROPERTIES = new ForgeFlowingFluid.Properties(
        LIVING_TERRA_FLUID_TYPE,
        LIVING_TERRA_SOURCE,
        LIVING_TERRA_FLOWING
    )
        .block(() -> (net.minecraft.world.level.block.LiquidBlock) AnimusBlocks.BLOCK_FLUID_LIVING_TERRA.get())
        .bucket(() -> AnimusItems.LIVING_TERRA_BUCKET.get());
}
