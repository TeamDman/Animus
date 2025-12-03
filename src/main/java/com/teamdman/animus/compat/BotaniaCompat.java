package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.ritual.Ritual;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Compatibility module for Botania
 * Handles mana integration with Blood Magic systems
 *
 * Features:
 * - Rune of Unleashed Nature (altar rune powered by mana)
 * - Sigil of Boundless Nature (LP-to-mana conversion)
 * - Diabolical Fungi (demon will-to-mana flower)
 * - Ritual of Floral Supremacy (supercharge Botania flowers)
 */
public class BotaniaCompat implements ICompatModule {

    private static BotaniaCompat INSTANCE;

    // DeferredRegister for Botania compatibility blocks
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.Mod.MODID);

    // DeferredRegister for Botania compatibility items
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, Constants.Mod.MODID);

    // DeferredRegister for Botania compatibility block entities
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.Mod.MODID);

    // Blocks
    public static final RegistryObject<Block> BLOCK_RUNE_UNLEASHED_NATURE = BLOCKS.register("rune_unleashed_nature",
        com.teamdman.animus.compat.botania.BlockRuneUnleashedNature::new);

    public static final RegistryObject<Block> BLOCK_DIABOLICAL_FUNGI = BLOCKS.register("diabolical_fungi",
        com.teamdman.animus.compat.botania.BlockDiabolicalFungi::new);

    // Block Items
    public static final RegistryObject<Item> ITEM_RUNE_UNLEASHED_NATURE = ITEMS.register("rune_unleashed_nature",
        () -> new BlockItem(BLOCK_RUNE_UNLEASHED_NATURE.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.animus.rune_unleashed_nature.flavour"));
                tooltip.add(Component.translatable("tooltip.animus.rune_unleashed_nature.info"));
                tooltip.add(Component.translatable("tooltip.animus.rune_unleashed_nature.capacity"));
                tooltip.add(Component.translatable("tooltip.animus.rune_unleashed_nature.orb"));
                tooltip.add(Component.translatable("tooltip.animus.rune_unleashed_nature.acceleration"));
                super.appendHoverText(stack, level, tooltip, flag);
            }
        });

    public static final RegistryObject<Item> ITEM_DIABOLICAL_FUNGI = ITEMS.register("diabolical_fungi",
        () -> new BlockItem(BLOCK_DIABOLICAL_FUNGI.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.animus.diabolical_fungi.flavour"));
                tooltip.add(Component.translatable("tooltip.animus.diabolical_fungi.info"));
                tooltip.add(Component.translatable("tooltip.animus.diabolical_fungi.conversion"));
                super.appendHoverText(stack, level, tooltip, flag);
            }
        });

    // Block Entities
    @SuppressWarnings("unchecked")
    public static final RegistryObject<BlockEntityType<com.teamdman.animus.compat.botania.BlockEntityRuneUnleashedNature>> RUNE_UNLEASHED_NATURE_BE =
        BLOCK_ENTITIES.register("rune_unleashed_nature",
            () -> BlockEntityType.Builder.of(
                com.teamdman.animus.compat.botania.BlockEntityRuneUnleashedNature::new,
                BLOCK_RUNE_UNLEASHED_NATURE.get()
            ).build(null));

    @SuppressWarnings("unchecked")
    public static final RegistryObject<BlockEntityType<com.teamdman.animus.compat.botania.BlockEntityDiabolicalFungi>> DIABOLICAL_FUNGI_BE =
        BLOCK_ENTITIES.register("diabolical_fungi",
            () -> BlockEntityType.Builder.of(
                com.teamdman.animus.compat.botania.BlockEntityDiabolicalFungi::new,
                BLOCK_DIABOLICAL_FUNGI.get()
            ).build(null));

    public BotaniaCompat() {
        INSTANCE = this;
    }

    public static BotaniaCompat getInstance() {
        return INSTANCE;
    }

    /**
     * Register the DeferredRegisters to the mod event bus
     * This must be called early, during mod construction
     */
    public static void registerDeferred(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        Animus.LOGGER.info("Registered Botania compatibility registries");
    }

    private static boolean ritualRegistered = false;

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Botania compatibility");

        // Register Rune of Unleashed Nature as a valid altar component
        registerAltarComponent();

        // Register Ritual of Floral Supremacy during server start (after Blood Magic's Patchouli registration)
        // This avoids the "multiblock already registered" conflict
        MinecraftForge.EVENT_BUS.register(RitualRegistrationHandler.class);

        // Register render layer for Diabolical Fungi (client-side only)
        // This is handled in AnimusClientSetup conditionally

        Animus.LOGGER.info("Botania compatibility initialized successfully");
    }

    /**
     * Handler class for delayed ritual registration
     */
    public static class RitualRegistrationHandler {
        @SubscribeEvent
        public static void onServerStarting(ServerStartingEvent event) {
            if (!ritualRegistered) {
                registerRitualDelayed();
                ritualRegistered = true;
            }
        }
    }

    /**
     * Register Rune of Unleashed Nature as a valid altar component
     */
    private void registerAltarComponent() {
        try {
            wayoftime.bloodmagic.impl.BloodMagicAPI.INSTANCE.registerAltarComponent(
                BLOCK_RUNE_UNLEASHED_NATURE.get().defaultBlockState(),
                wayoftime.bloodmagic.altar.ComponentType.BLOODRUNE.name()
            );
            Animus.LOGGER.info("Registered Rune of Unleashed Nature as BLOODRUNE component for Blood Magic altars");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Rune of Unleashed Nature with Blood Magic", e);
        }
    }

    /**
     * Register Ritual of Floral Supremacy using reflection
     * Blood Magic doesn't expose a public API for programmatic ritual registration,
     * so we need to access the private maps in RitualManager.
     *
     * This is called during ServerStartingEvent to ensure it runs AFTER Blood Magic's
     * Patchouli multiblock registration (which happens during FMLLoadCompleteEvent).
     */
    @SuppressWarnings("unchecked")
    private static void registerRitualDelayed() {
        try {
            Ritual ritual = new com.teamdman.animus.compat.botania.RitualFloralSupremacy();
            String ritualId = Constants.Rituals.FLORAL_SUPREMACY;

            // Get the private 'rituals' map from RitualManager
            Field ritualsField = BloodMagic.RITUAL_MANAGER.getClass().getDeclaredField("rituals");
            ritualsField.setAccessible(true);
            Map<String, Ritual> rituals = (Map<String, Ritual>) ritualsField.get(BloodMagic.RITUAL_MANAGER);

            // Get the private 'ritualsReverse' map from RitualManager
            Field ritualsReverseField = BloodMagic.RITUAL_MANAGER.getClass().getDeclaredField("ritualsReverse");
            ritualsReverseField.setAccessible(true);
            Map<Ritual, String> ritualsReverse = (Map<Ritual, String>) ritualsReverseField.get(BloodMagic.RITUAL_MANAGER);

            // Get the private 'sortedRituals' list from RitualManager
            Field sortedRitualsField = BloodMagic.RITUAL_MANAGER.getClass().getDeclaredField("sortedRituals");
            sortedRitualsField.setAccessible(true);
            java.util.List<Ritual> sortedRituals = (java.util.List<Ritual>) sortedRitualsField.get(BloodMagic.RITUAL_MANAGER);

            // Register the ritual
            rituals.put(ritualId, ritual);
            ritualsReverse.put(ritual, ritualId);
            sortedRituals.add(ritual);

            Animus.LOGGER.info("Registered Ritual of Floral Supremacy with Blood Magic");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Ritual of Floral Supremacy", e);
        }
    }

    @Override
    public String getModId() {
        return "botania";
    }
}
