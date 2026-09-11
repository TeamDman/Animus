package com.teamdman.animus;

import com.teamdman.animus.advancements.AnimusCriteriaTriggers;
import com.teamdman.animus.compat.CompatHandler;
import com.teamdman.animus.rituals.RitualPersistence;
import com.teamdman.animus.network.AnimusNetwork;
import com.teamdman.animus.registry.*;
import com.teamdman.animus.worldgen.AnimusTreeDecoratorTypes;
import net.minecraftforge.common.world.ForgeChunkManager;
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

        // Register compatibility module deferred registers
        CompatHandler.registerDeferredRegisters(modEventBus);

        // Register event listeners
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Animus mod loading...");
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        LOGGER.info("Animus common setup");

        // Register network packets
        AnimusNetwork.register();

        event.enqueueWork(AnimusCriteriaTriggers::register);
        event.enqueueWork(() -> ForgeChunkManager.setForcedChunkLoadingCallback(
            Constants.Mod.MODID, RitualPersistence::validateTickets));

        // Initialize compatibility modules for optional mod integrations
        CompatHandler.init();

        // Register Crystallized Demon Will block as a valid CRYSTAL component for tier 6 altars
        try {
            wayoftime.bloodmagic.impl.BloodMagicAPI.INSTANCE.registerAltarComponent(
                AnimusBlocks.BLOCK_CRYSTALLIZED_DEMON_WILL.get().defaultBlockState(),
                wayoftime.bloodmagic.altar.ComponentType.CRYSTAL.name()
            );
            LOGGER.info("Registered Crystallized Demon Will Block as CRYSTAL component for Blood Magic altars");
        } catch (Exception e) {
            LOGGER.error("Failed to register Crystallized Demon Will Block with Blood Magic", e);
        }

        // Note: Arcane Rune and Rune of Unleashed Nature altar components are registered
        // in their respective compat modules (ArsNouveauCompat, BotaniaCompat)

        event.enqueueWork(() -> {
            // Register Transcendent Blood Orb to the OrbRegistry tierMap
            // This allows it to be used as a valid recipe ingredient for any tier 1-6 blood orb recipes
            try {
                wayoftime.bloodmagic.core.registry.OrbRegistry.tierMap.put(
                    AnimusBloodOrbs.BLOOD_ORB_TRANSCENDENT.get().getTier(),
                    new net.minecraft.world.item.ItemStack(AnimusItems.BLOOD_ORB_TRANSCENDENT.get())
                );
                LOGGER.info("Registered Transcendent Blood Orb to OrbRegistry tierMap");
            } catch (Exception e) {
                LOGGER.error("Failed to register Transcendent Blood Orb to OrbRegistry", e);
            }

            // Register rituals here
            // Register other things that need to happen during setup
        });
    }
}
