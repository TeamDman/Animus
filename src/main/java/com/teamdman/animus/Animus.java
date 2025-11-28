package com.teamdman.animus;

import com.teamdman.animus.registry.*;
import com.teamdman.animus.worldgen.AnimusTreeDecoratorTypes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Constants.Mod.MODID)
public class Animus {
    public static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("removal")
    public Animus() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        AnimusConfig.register(ModLoadingContext.get());

        // Register all deferred registers
        AnimusBlocks.BLOCKS.register(modEventBus);
        AnimusItems.ITEMS.register(modEventBus);
        AnimusBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        AnimusEntityTypes.ENTITY_TYPES.register(modEventBus);
        AnimusFluids.FLUID_TYPES.register(modEventBus);
        AnimusFluids.FLUIDS.register(modEventBus);
        AnimusMobEffects.MOB_EFFECTS.register(modEventBus);
        AnimusSounds.SOUNDS.register(modEventBus);
        AnimusCreativeTabs.CREATIVE_TABS.register(modEventBus);
        AnimusTreeDecoratorTypes.TREE_DECORATOR_TYPES.register(modEventBus);
        AnimusRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        // Register event listeners
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Animus mod loading...");
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        LOGGER.info("Animus common setup");
        event.enqueueWork(() -> {
            // Register strippable blocks (axe interaction) using reflection
            try {
                java.lang.reflect.Field strippablesField = net.minecraft.world.item.AxeItem.class.getDeclaredField("STRIPPABLES");
                strippablesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block> strippables =
                    (java.util.Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block>) strippablesField.get(null);

                // Create new map with our addition
                java.util.Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.Block> newStrippables =
                    new java.util.HashMap<>(strippables);
                newStrippables.put(AnimusBlocks.BLOCK_BLOOD_WOOD.get(), AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get());

                // Replace the field
                strippablesField.set(null, newStrippables);
                LOGGER.info("Registered blood wood as strippable");
            } catch (Exception e) {
                LOGGER.error("Failed to register strippable blocks", e);
            }

            // Register rituals here
            // Register other things that need to happen during setup
        });
    }
}
