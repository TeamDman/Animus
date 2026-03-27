package com.breakinblocks.animusnv;

import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.network.AnimusPayloads;
import com.breakinblocks.animusnv.registry.*;
import com.breakinblocks.animusnv.worldgen.AnimusTreeDecoratorTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.Mod.MODID)
public class Animus {
    public static final Logger LOGGER = LogManager.getLogger();

    public Animus(IEventBus modEventBus, ModContainer modContainer) {
        AnimusStartupConfig.register(modContainer);
        AnimusConfig.register(modContainer);

        AnimusBlocks.BLOCKS.register(modEventBus);
        AnimusItems.ITEMS.register(modEventBus);
        AnimusBloodOrbs.BLOOD_ORBS.register(modEventBus);
        AnimusBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        AnimusEntityTypes.ENTITY_TYPES.register(modEventBus);
        AnimusFluids.FLUID_TYPES.register(modEventBus);
        AnimusFluids.FLUIDS.register(modEventBus);
        AnimusFluids.registerClientExtensionsListener(modEventBus);
        AnimusMobEffects.MOB_EFFECTS.register(modEventBus);
        AnimusSounds.SOUNDS.register(modEventBus);
        AnimusCreativeTabs.CREATIVE_TABS.register(modEventBus);
        AnimusTreeDecoratorTypes.TREE_DECORATOR_TYPES.register(modEventBus);
        AnimusRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        AnimusAttributes.ATTRIBUTES.register(modEventBus);
        AnimusDataComponents.DATA_COMPONENTS.register(modEventBus);

        AnimusSigilEffects.register(modEventBus);

        // Force class loading to ensure all registrations are queued
        AnimusRituals.init();

        AnimusRituals.RITUALS.register(modEventBus);
        AnimusRituals.IMPERFECT_RITUALS.register(modEventBus);

        CompatHandler.registerDeferredRegisters(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        LOGGER.debug("Animus mod loading...");
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        AnimusPayloads.register(event);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.debug("Animus common setup");

        CompatHandler.init();

        // Note: In NeoVitae 1.21.1, altar components are data-driven.
        // Crystallized Spiritus Block registration is handled via data/animus/data_maps/

        // Note: Blood orb stats are now handled via DataMaps
        // See data/animus/data_maps/item/blood_orb_stats.json
    }
}
