package com.breakinblocks.animusnv;

import com.breakinblocks.animusnv.advancements.AnimusCriteriaTriggers;
import java.lang.reflect.Method;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.EvilCraftCompat;
import com.breakinblocks.animusnv.network.AnimusPayloads;
import com.breakinblocks.animusnv.registry.*;
import com.breakinblocks.animusnv.worldgen.AnimusTreeDecoratorTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
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
        AnimusCriteriaTriggers.register(modEventBus);

        CompatHandler.registerDeferredRegisters(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::registerCapabilities);

        wireGameTests(modEventBus);

        LOGGER.debug("Animus mod loading...");
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        AnimusPayloads.register(event);
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent event) {
        if (ModList.get().isLoaded("evilcraft")) {
            registerEvilCraftCapabilities(event);
        }
    }

    private void registerEvilCraftCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.Fluid.BLOCK,
            EvilCraftCompat.SANGUINE_RECTIFIER_BE.get(),
            (be, direction) -> be.getBloodTank()
        );
    }

    /**
     * The gametest source set is only on the classpath in development, so it is wired
     * reflectively and quietly skipped in production builds.
     */
    private static void wireGameTests(IEventBus modBus) {
        try {
            Class<?> registration = Class.forName("com.breakinblocks.animusnv.gametest.AnimusGameTestRegistration");
            Method handler = registration.getMethod("registerTests", RegisterGameTestsEvent.class);
            modBus.addListener(RegisterGameTestsEvent.class, event -> {
                try {
                    handler.invoke(null, event);
                } catch (ReflectiveOperationException t) {
                    LOGGER.error("Failed to invoke AnimusGameTestRegistration.registerTests", t);
                }
            });
        } catch (ClassNotFoundException expected) {
            // Test source set not on classpath - production build
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to wire gametest hooks", e);
        }
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
