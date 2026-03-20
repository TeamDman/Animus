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

    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(Registries.BLOCK, Constants.Mod.MODID);

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, Constants.Mod.MODID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.Mod.MODID);

    public static final DeferredRegister<ImperfectRitual> IMPERFECT_RITUALS =
        DeferredRegister.create(RitualRegistry.IMPERFECT_RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    public static final DeferredHolder<ImperfectRitual, RitualMagi> MAGI =
        IMPERFECT_RITUALS.register(Constants.Rituals.MAGI, () -> new RitualMagi());

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

    public static void registerDeferred(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        IMPERFECT_RITUALS.register(modEventBus);
        Animus.LOGGER.debug("Registered Ars Nouveau compatibility registries (blocks, items, block entities, imperfect rituals)");
    }

    @Override
    public void init() {
        Animus.LOGGER.debug("Initializing Ars Nouveau compatibility");

        SourceAttunementHandler.register();
        Animus.LOGGER.debug("Registered Source Attunement Living Armor upgrade handler");

        LivingArmorGlyphHandler.register();
        Animus.LOGGER.debug("Registered Living Armor Glyph Handler");

        ArcaneRuneEventHandler.register();
        Animus.LOGGER.debug("Registered Arcane Rune event handler");

        // Register as basic speed rune so it's recognized in altar structure;
        // dynamic bonuses based on Source are applied by ArcaneRuneEventHandler
        registerArcaneRuneWithBloodMagic();

        Animus.LOGGER.debug("Ars Nouveau compatibility initialized successfully");
    }

    private void registerArcaneRuneWithBloodMagic() {
        try {
            IAltarRuneRegistry registry = NeoVitaeAPI.getInstance().getRuneRegistry();

            // Amount 1 ensures the block is recognized as a rune;
            // event handler overrides with dynamic values
            registry.registerRuneBlock(ARCANE_RUNE.get(), EnumAltarRuneType.SPEED, 1);

            Animus.LOGGER.debug("Registered Arcane Rune with NeoVitae altar rune registry");
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register Arcane Rune with NeoVitae: {}", e.getMessage());
        }
    }

    @Override
    public String getModId() {
        return "ars_nouveau";
    }
}
