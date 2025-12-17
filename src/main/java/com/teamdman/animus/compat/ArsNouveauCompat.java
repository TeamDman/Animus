package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.arsnouveau.ArcaneRuneEventHandler;
import com.teamdman.animus.compat.arsnouveau.BlockArcaneRune;
import com.teamdman.animus.compat.arsnouveau.BlockEntityArcaneRune;
import com.teamdman.animus.compat.arsnouveau.LivingArmorGlyphHandler;
import com.teamdman.animus.compat.arsnouveau.RitualMagi;
import com.teamdman.animus.compat.arsnouveau.SourceAttunementHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.altar.rune.EnumAltarRuneType;
import com.breakinblocks.neovitae.api.altar.rune.IAltarRuneRegistry;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;
import com.breakinblocks.neovitae.ritual.RitualRegistry;

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

    // DeferredRegister for Ars Nouveau compatibility imperfect rituals
    public static final DeferredRegister<ImperfectRitual> IMPERFECT_RITUALS =
        DeferredRegister.create(RitualRegistry.IMPERFECT_RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    // ===== Imperfect Rituals =====

    // Imperfect Ritual of the Magi - grants Mana Regen effect
    // Requires Source Jar on Imperfect Ritual Stone
    public static final DeferredHolder<ImperfectRitual, RitualMagi> MAGI =
        IMPERFECT_RITUALS.register(Constants.Rituals.MAGI, () -> new RitualMagi());

    // ===== Blocks =====

    // Arcane Rune - An Ars Nouveau powered altar component
    // Using lambda instead of method reference to defer class loading
    public static final DeferredHolder<Block, BlockArcaneRune> ARCANE_RUNE = BLOCKS.register(
        "arcane_rune", () -> new BlockArcaneRune());

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
        IMPERFECT_RITUALS.register(modEventBus);
        Animus.LOGGER.info("Registered Ars Nouveau compatibility registries (blocks, items, block entities, imperfect rituals)");
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Ars Nouveau compatibility");

        // Register Source Attunement Living Armor upgrade event handlers
        // The upgrade itself is defined in data/animus/neovitae/living_upgrades/source_attunement.json
        SourceAttunementHandler.register();
        Animus.LOGGER.info("Registered Source Attunement Living Armor upgrade handler");

        // Register Living Armor XP handler for spell casting
        LivingArmorGlyphHandler.register();
        Animus.LOGGER.info("Registered Living Armor Glyph Handler");

        // Register Arcane Rune event handler for dynamic altar bonuses
        ArcaneRuneEventHandler.register();
        Animus.LOGGER.info("Registered Arcane Rune event handler");

        // Register Arcane Rune with Blood Magic's rune registry
        // This registers the block as a basic speed rune (amount 1) so it's recognized
        // as a valid rune in the altar structure. The dynamic bonuses based on Source
        // availability are handled by the ArcaneRuneEventHandler.
        registerArcaneRuneWithBloodMagic();

        Animus.LOGGER.info("Ars Nouveau compatibility initialized successfully");
    }

    /**
     * Register the Arcane Rune block with Blood Magic's altar rune registry.
     *
     * The Arcane Rune is registered as a SPEED rune with amount 0 (no base bonus).
     * The actual bonuses are applied dynamically by the ArcaneRuneEventHandler
     * based on the rune's Source availability.
     */
    private void registerArcaneRuneWithBloodMagic() {
        try {
            IAltarRuneRegistry registry = NeoVitaeAPI.getInstance().getRuneRegistry();

            // Register with SPEED rune type, amount 0 (no inherent bonus)
            // The event handler applies the actual bonuses based on Source state
            // We use amount 1 with SPEED to ensure the block is recognized as a rune,
            // but the event handler will override this with dynamic values
            registry.registerRuneBlock(ARCANE_RUNE.get(), EnumAltarRuneType.SPEED, 1);

            Animus.LOGGER.info("Registered Arcane Rune with Blood Magic altar rune registry");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Arcane Rune with Blood Magic: {}", e.getMessage());
        }
    }

    @Override
    public String getModId() {
        return "ars_nouveau";
    }
}
