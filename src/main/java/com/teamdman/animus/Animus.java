package com.teamdman.animus;

import com.teamdman.animus.compat.CompatHandler;
import com.teamdman.animus.network.AnimusPayloads;
import com.teamdman.animus.registry.*;
import com.teamdman.animus.worldgen.AnimusTreeDecoratorTypes;
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
        // Register config
        AnimusConfig.register(modContainer);

        // Register all deferred registers
        AnimusBlocks.BLOCKS.register(modEventBus);
        AnimusItems.ITEMS.register(modEventBus);
        AnimusBloodOrbs.BLOOD_ORBS.register(modEventBus);
        AnimusBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        AnimusEntityTypes.ENTITY_TYPES.register(modEventBus);
        AnimusFluids.FLUID_TYPES.register(modEventBus);
        AnimusFluids.FLUIDS.register(modEventBus);
        AnimusMobEffects.MOB_EFFECTS.register(modEventBus);
        AnimusSounds.SOUNDS.register(modEventBus);
        AnimusCreativeTabs.CREATIVE_TABS.register(modEventBus);
        AnimusTreeDecoratorTypes.TREE_DECORATOR_TYPES.register(modEventBus);
        AnimusRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        AnimusRecipeTypes.RECIPE_TYPES.register(modEventBus);
        AnimusAttributes.ATTRIBUTES.register(modEventBus);
        AnimusDataComponents.DATA_COMPONENTS.register(modEventBus);

        // Register rituals to Blood Magic's registry
        // Note: Imperfect rituals are now internal to Blood Magic
        AnimusRituals.RITUALS.register(modEventBus);

        // Register compatibility module deferred registers
        CompatHandler.registerDeferredRegisters(modEventBus);

        // Register event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        LOGGER.info("Animus mod loading...");
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        AnimusPayloads.register(event);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Animus common setup");

        // Initialize compatibility modules for optional mod integrations
        CompatHandler.init();

        // Note: In Blood Magic 1.21.1, altar components are data-driven.
        // Crystallized Demon Will Block registration is handled via data/animus/data_maps/

        // Note: Blood orb stats are now handled via DataMaps
        // See data/animus/data_maps/item/blood_orb_stats.json

        event.enqueueWork(() -> {
            // Deferred initialization tasks can go here
            LOGGER.info("Animus deferred setup complete");
        });
    }
}
