package com.breakinblocks.animusnv.client;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusFluids;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSources;

@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.Mod.MODID)
public class AnimusFluidModels {

    private static final int ANTILIFE_TINT = 0xFFEEEEEE;
    private static final int LIVING_TERRA_TINT = 0xFF8B6F47;

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(
            new FluidModel.Unbaked(
                new Material(Constants.Resource.fluidAntiLifeStill),
                new Material(Constants.Resource.fluidAntiLifeFlowing),
                null,
                FluidTintSources.constant(ANTILIFE_TINT)),
            AnimusFluids.ANTILIFE_SOURCE,
            AnimusFluids.ANTILIFE_FLOWING);

        event.register(
            new FluidModel.Unbaked(
                new Material(Constants.Resource.fluidLivingTerraStill),
                new Material(Constants.Resource.fluidLivingTerraFlowing),
                null,
                FluidTintSources.constant(LIVING_TERRA_TINT)),
            AnimusFluids.LIVING_TERRA_SOURCE,
            AnimusFluids.LIVING_TERRA_FLOWING);
    }
}
