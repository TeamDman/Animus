package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.dynamictrees.BloodCoreGenFeature;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.dtteam.dynamictrees.event.RegistryEvent;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.systems.BranchConnectables;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class DynamicTreesCompat implements ICompatModule {

    public static final ResourceLocation BLOOD_SPECIES =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "bloodwood");
    public static final ResourceLocation BLOOD_CORE_FEATURE =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "blood_core");

    private static final int SPREAD_GROWTH_PULSES = 8;

    public static void registerDeferred(IEventBus modEventBus) {
        NeoForgeRegistryHandler.setup(Constants.Mod.MODID, modEventBus);
        modEventBus.register(DynamicTreesCompat.class);
    }

    @SubscribeEvent
    public static void onGenFeatureRegistry(RegistryEvent<GenFeature> event) {
        if (event.isEntryOfType(GenFeature.class)) {
            event.getRegistry().register(new BloodCoreGenFeature(BLOOD_CORE_FEATURE));
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> BranchConnectables.makeBlockConnectable(
            AnimusBlocks.BLOCK_BLOOD_CORE.get(), BloodCoreGenFeature::connectionRadius));
    }

    public static boolean plantBloodTree(ServerLevel level, BlockPos pos) {
        Species species = Species.REGISTRY.get(BLOOD_SPECIES);
        if (!species.isValid()) {
            return false;
        }

        if (!level.getBlockState(pos).isAir()) {
            level.removeBlock(pos, false);
        }

        if (!species.transitionToTree(level, pos)) {
            return false;
        }

        BlockPos rootPos = pos.below();
        for (int i = 0; i < SPREAD_GROWTH_PULSES; i++) {
            TreeHelper.growPulse(level, rootPos);
        }
        return true;
    }

    @Override
    public void init() {
        Animus.LOGGER.debug("Initializing Dynamic Trees compatibility");
    }

    @Override
    public String getModId() {
        return "dynamictrees";
    }
}
