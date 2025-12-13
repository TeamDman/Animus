package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.client.AntiLifeFluidClientExtension;
import com.teamdman.animus.client.LivingTerraFluidClientExtension;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Consumer;

public class AnimusFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Constants.Mod.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(Registries.FLUID, Constants.Mod.MODID);

    // AntiLife Fluid Type
    public static final DeferredHolder<FluidType, FluidType> ANTILIFE_FLUID_TYPE = FLUID_TYPES.register(
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
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    consumer.accept(AntiLifeFluidClientExtension.INSTANCE);
                }
            }
        }
    );

    // Living Terra Fluid Type
    public static final DeferredHolder<FluidType, FluidType> LIVING_TERRA_FLUID_TYPE = FLUID_TYPES.register(
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
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    consumer.accept(LivingTerraFluidClientExtension.INSTANCE);
                }
            }
        }
    );

    // AntiLife Fluids
    public static final DeferredHolder<Fluid, FlowingFluid> ANTILIFE_FLOWING = FLUIDS.register(
        "antilife_flowing",
        () -> new BaseFlowingFluid.Flowing(AnimusFluids.ANTILIFE_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> ANTILIFE_SOURCE = FLUIDS.register(
        "antilife",
        () -> new BaseFlowingFluid.Source(AnimusFluids.ANTILIFE_PROPERTIES)
    );

    // Living Terra Fluids
    public static final DeferredHolder<Fluid, FlowingFluid> LIVING_TERRA_FLOWING = FLUIDS.register(
        "living_terra_flowing",
        () -> new BaseFlowingFluid.Flowing(AnimusFluids.LIVING_TERRA_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> LIVING_TERRA_SOURCE = FLUIDS.register(
        "living_terra",
        () -> new BaseFlowingFluid.Source(AnimusFluids.LIVING_TERRA_PROPERTIES)
    );

    // Fluid Properties
    public static final BaseFlowingFluid.Properties ANTILIFE_PROPERTIES = new BaseFlowingFluid.Properties(
        ANTILIFE_FLUID_TYPE,
        ANTILIFE_SOURCE,
        ANTILIFE_FLOWING
    )
        .block(() -> (LiquidBlock) AnimusBlocks.BLOCK_FLUID_ANTILIFE.get())
        .bucket(() -> AnimusItems.ANTILIFE_BUCKET.get());

    public static final BaseFlowingFluid.Properties LIVING_TERRA_PROPERTIES = new BaseFlowingFluid.Properties(
        LIVING_TERRA_FLUID_TYPE,
        LIVING_TERRA_SOURCE,
        LIVING_TERRA_FLOWING
    )
        .block(() -> (LiquidBlock) AnimusBlocks.BLOCK_FLUID_LIVING_TERRA.get())
        .bucket(() -> AnimusItems.LIVING_TERRA_BUCKET.get());
}
