package com.breakinblocks.animusnv.registry;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.client.AntiLifeFluidClientExtension;
import com.breakinblocks.animusnv.client.LivingTerraFluidClientExtension;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class AnimusFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Constants.Mod.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(Registries.FLUID, Constants.Mod.MODID);

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
            .descriptionId("fluid.animusnv.antilife")
        )
    );

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
            .descriptionId("fluid.animusnv.living_terra")
        )
    );

    public static final DeferredHolder<Fluid, FlowingFluid> ANTILIFE_FLOWING = FLUIDS.register(
        "antilife_flowing",
        () -> new BaseFlowingFluid.Flowing(AnimusFluids.ANTILIFE_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> ANTILIFE_SOURCE = FLUIDS.register(
        "antilife",
        () -> new BaseFlowingFluid.Source(AnimusFluids.ANTILIFE_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> LIVING_TERRA_FLOWING = FLUIDS.register(
        "living_terra_flowing",
        () -> new BaseFlowingFluid.Flowing(AnimusFluids.LIVING_TERRA_PROPERTIES)
    );

    public static final DeferredHolder<Fluid, FlowingFluid> LIVING_TERRA_SOURCE = FLUIDS.register(
        "living_terra",
        () -> new BaseFlowingFluid.Source(AnimusFluids.LIVING_TERRA_PROPERTIES)
    );

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

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(AntiLifeFluidClientExtension.INSTANCE, ANTILIFE_FLUID_TYPE);
        event.registerFluidType(LivingTerraFluidClientExtension.INSTANCE, LIVING_TERRA_FLUID_TYPE);
    }

    public static void registerClientExtensionsListener(IEventBus modBus) {
        modBus.addListener(AnimusFluids::registerClientExtensions);
    }
}
