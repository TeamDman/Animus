package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.arsnouveau.BlockArcaneRune;
import com.teamdman.animus.compat.arsnouveau.BlockEntityArcaneRune;
import com.teamdman.animus.compat.arsnouveau.LivingArmorGlyphHandler;
import com.teamdman.animus.compat.arsnouveau.SourceAttunementHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Compatibility module for Ars Nouveau
 * Handles Living Armor integration with Ars Nouveau spell system
 *
 * Features:
 * - Living Armor gains XP from casting glyphs
 * - Source Attunement upgrade tree for Ars Nouveau spellcasters
 * - Arcane Rune block that consumes Source for altar bonuses
 */
public class ArsNouveauCompat implements ICompatModule {

    private static ArsNouveauCompat INSTANCE;

    // DeferredRegister for Ars Nouveau compatibility blocks
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(Registries.BLOCK, Constants.Mod.MODID);

    // DeferredRegister for Ars Nouveau compatibility items
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, Constants.Mod.MODID);

    // DeferredRegister for Ars Nouveau compatibility block entities
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.Mod.MODID);

    // Arcane Rune - An Ars Nouveau powered altar component
    public static final DeferredHolder<Block, BlockArcaneRune> ARCANE_RUNE = BLOCKS.register(
        "arcane_rune", BlockArcaneRune::new);

    public static final DeferredHolder<Item, Item> ARCANE_RUNE_ITEM = ITEMS.register(
        "arcane_rune", () -> new BlockItem(ARCANE_RUNE.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityArcaneRune>> ARCANE_RUNE_BE =
        BLOCK_ENTITIES.register("arcane_rune", () -> BlockEntityType.Builder.of(
            BlockEntityArcaneRune::new, ARCANE_RUNE.get()
        ).build(null));

    public ArsNouveauCompat() {
        INSTANCE = this;
    }

    public static ArsNouveauCompat getInstance() {
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
        Animus.LOGGER.info("Registered Ars Nouveau compatibility registries");
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Ars Nouveau compatibility");

        // Register Source Attunement Living Armor upgrade event handlers
        // The upgrade itself is defined in data/animus/bloodmagic/living_upgrades/source_attunement.json
        SourceAttunementHandler.register();
        Animus.LOGGER.info("Registered Source Attunement Living Armor upgrade handler");

        // Register Living Armor XP handler for spell casting
        LivingArmorGlyphHandler.register();
        Animus.LOGGER.info("Registered Living Armor Glyph Handler");

        // Arcane Rune block is registered via DeferredRegister
        // TODO: Register Arcane Rune as an altar component (similar to Speed/Dislocation rune)
        // This requires Blood Magic's altar component system which may need investigation

        Animus.LOGGER.info("Ars Nouveau compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "ars_nouveau";
    }
}
