package com.teamdman.animus;

import com.teamdman.animus.registry.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.Mod.MODID)
public class Animus {
    public static final Logger LOGGER = LogManager.getLogger();
    public static Animus instance;

    public Animus(IEventBus modEventBus, ModLoadingContext modLoadingContext) {
        instance = this;

        // Register config
        AnimusConfig.register(modLoadingContext);

        // Register all deferred registers
        AnimusBlocks.BLOCKS.register(modEventBus);
        AnimusItems.ITEMS.register(modEventBus);
        AnimusBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        AnimusFluids.FLUID_TYPES.register(modEventBus);
        AnimusFluids.FLUIDS.register(modEventBus);
        AnimusMobEffects.MOB_EFFECTS.register(modEventBus);
        AnimusSounds.SOUNDS.register(modEventBus);
        AnimusCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Register event listeners
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Animus mod loading...");
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        LOGGER.info("Animus common setup");
        event.enqueueWork(() -> {
            // Register rituals here
            // Register other things that need to happen during setup
        });
    }
}
