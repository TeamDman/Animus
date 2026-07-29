package com.breakinblocks.animusnv.registry;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.items.*;
import com.breakinblocks.animusnv.items.ItemReagent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.breakinblocks.neovitae.common.item.sigil.SigilItem;
import com.breakinblocks.neovitae.registry.SigilTypeRegistry;


import java.util.function.Consumer;

public class AnimusItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.Mod.MODID);

    public static final DeferredHolder<Item, AnimusGuideBookItem> GUIDE_BOOK = ITEMS.registerItem("guide_book", AnimusGuideBookItem::new);

    private static DeferredHolder<Item, Item> registerBlockItem(String name, DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.registerItem(name, props -> new BlockItem(block.get(), props), Item.Properties::useBlockDescriptionPrefix);
    }

    private static DeferredHolder<Item, Item> registerSigil(String name, String sigilTypePath) {
        return ITEMS.registerItem(name, props -> new SigilItem(props,
                SigilTypeRegistry.key(Identifier.fromNamespaceAndPath(Constants.Mod.MODID, sigilTypePath))
        ));
    }

    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD = registerBlockItem("blood_wood", AnimusBlocks.BLOCK_BLOOD_WOOD);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_STRIPPED = registerBlockItem("blood_wood_stripped", AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_PLANKS = registerBlockItem("blood_wood_planks", AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_STAIRS = registerBlockItem("blood_wood_stairs", AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_SLAB = registerBlockItem("blood_wood_slab", AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_FENCE = registerBlockItem("blood_wood_fence", AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_FENCE_GATE = registerBlockItem("blood_wood_fence_gate", AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE);

    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_SAPLING = ITEMS.registerItem("blood_sapling",
        props -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_SAPLING.get(), props) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_SAPLING_FLAVOUR));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_SAPLING_INFO));
                super.appendHoverText(stack, context, display, tooltip, flag);
            }
        }, Item.Properties::useBlockDescriptionPrefix);

    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_CORE = ITEMS.registerItem("blood_core",
        props -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_CORE.get(), props) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_FLAVOUR));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_INFO));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_MULTIBLOCK));
                super.appendHoverText(stack, context, display, tooltip, flag);
            }
        }, Item.Properties::useBlockDescriptionPrefix);

    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_LEAVES = registerBlockItem("blood_leaves", AnimusBlocks.BLOCK_BLOOD_LEAVES);
    public static final DeferredHolder<Item, Item> BLOCK_ANTILIFE = registerBlockItem("antilife", AnimusBlocks.BLOCK_ANTILIFE);

    public static final DeferredHolder<Item, Item> BLOCK_CRYSTALLIZED_SPIRITUS = ITEMS.registerItem("crystallized_spiritus_block",
        props -> new BlockItem(AnimusBlocks.BLOCK_CRYSTALLIZED_SPIRITUS.get(), props) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_SPIRITUS_FLAVOUR));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_SPIRITUS_INFO));
                tooltip.accept(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_SPIRITUS_ALTAR));
                super.appendHoverText(stack, context, display, tooltip, flag);
            }
        }, Item.Properties::useBlockDescriptionPrefix);

    // Imperfect Ritual Stone removed - use NeoVitae's native NVBlocks.IMPERFECT_RITUAL_STONE instead

    // Sanguine Rectifier moved to EvilCraftCompat (only exists when EvilCraft is loaded)

    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE = registerBlockItem("willful_stone", AnimusBlocks.BLOCK_WILLFUL_STONE);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_WHITE = registerBlockItem("willful_stone_white", AnimusBlocks.BLOCK_WILLFUL_STONE_WHITE);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_ORANGE = registerBlockItem("willful_stone_orange", AnimusBlocks.BLOCK_WILLFUL_STONE_ORANGE);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_MAGENTA = registerBlockItem("willful_stone_magenta", AnimusBlocks.BLOCK_WILLFUL_STONE_MAGENTA);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_LIGHT_BLUE = registerBlockItem("willful_stone_light_blue", AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_BLUE);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_YELLOW = registerBlockItem("willful_stone_yellow", AnimusBlocks.BLOCK_WILLFUL_STONE_YELLOW);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_LIME = registerBlockItem("willful_stone_lime", AnimusBlocks.BLOCK_WILLFUL_STONE_LIME);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_PINK = registerBlockItem("willful_stone_pink", AnimusBlocks.BLOCK_WILLFUL_STONE_PINK);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_LIGHT_GRAY = registerBlockItem("willful_stone_light_gray", AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_GRAY);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_CYAN = registerBlockItem("willful_stone_cyan", AnimusBlocks.BLOCK_WILLFUL_STONE_CYAN);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_PURPLE = registerBlockItem("willful_stone_purple", AnimusBlocks.BLOCK_WILLFUL_STONE_PURPLE);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_BLUE = registerBlockItem("willful_stone_blue", AnimusBlocks.BLOCK_WILLFUL_STONE_BLUE);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_BROWN = registerBlockItem("willful_stone_brown", AnimusBlocks.BLOCK_WILLFUL_STONE_BROWN);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_GREEN = registerBlockItem("willful_stone_green", AnimusBlocks.BLOCK_WILLFUL_STONE_GREEN);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_RED = registerBlockItem("willful_stone_red", AnimusBlocks.BLOCK_WILLFUL_STONE_RED);
    public static final DeferredHolder<Item, Item> BLOCK_WILLFUL_STONE_BLACK = registerBlockItem("willful_stone_black", AnimusBlocks.BLOCK_WILLFUL_STONE_BLACK);

    public static final DeferredHolder<Item, Item> BLOOD_APPLE = ITEMS.registerItem("blood_apple",
        ItemBloodApple::new);

    public static final DeferredHolder<Item, Item> FRAGMENT_HEALING = ITEMS.registerItem("fragment_healing",
        ItemFragmentHealing::new);

    public static final DeferredHolder<Item, ItemBloodOrbTranscendent> BLOOD_ORB_TRANSCENDENT = AnimusBloodOrbs.BLOOD_ORB_TRANSCENDENT;

    public static final DeferredHolder<Item, Item> MOBSOUL = ITEMS.registerItem("mob_soul",
        ItemMobSoul::new);

    public static final DeferredHolder<Item, Item> REAGENT_BUILDER = ITEMS.registerItem("reagentbuilder",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_CHAINS = ITEMS.registerItem("reagentchains",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_CONSUMPTION = ITEMS.registerItem("reagentconsumption",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_LEACH = ITEMS.registerItem("reagentleach",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_STORM = ITEMS.registerItem("reagentstorm",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_TRANSPOSITION = ITEMS.registerItem("reagenttransposition",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_BOUNDLESS_NATURE = ITEMS.registerItem("reagentboundlessnature",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_EQUIVALENCY = ITEMS.registerItem("reagentequivalency",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_FREE_SOUL = ITEMS.registerItem("reagentfreesoul",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_HEAVENLY_WRATH = ITEMS.registerItem("reagentheavelywrath",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_REMEDIUM = ITEMS.registerItem("reagentremendium",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_REPARARE = ITEMS.registerItem("reagentreparare",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_TEMPORAL_DOMINANCE = ITEMS.registerItem("reagenttemporaldominance",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_FIST = ITEMS.registerItem("reagentfist",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> SIGIL_BUILDER = registerSigil("sigil_builder", "builder");
    public static final DeferredHolder<Item, Item> SIGIL_CHAINS = registerSigil("sigil_chains", "chains");
    public static final DeferredHolder<Item, Item> SIGIL_CONSUMPTION = registerSigil("sigil_consumption", "consumption");
    public static final DeferredHolder<Item, Item> SIGIL_LEACH = registerSigil("sigil_leach", "leach");
    public static final DeferredHolder<Item, Item> SIGIL_STORM = registerSigil("sigil_storm", "storm");
    public static final DeferredHolder<Item, Item> SIGIL_HEAVENLY_WRATH = registerSigil("sigil_heavenly_wrath", "heavenly_wrath");
    public static final DeferredHolder<Item, Item> SIGIL_REMEDIUM = registerSigil("sigil_remedium", "remedium");
    public static final DeferredHolder<Item, Item> SIGIL_REPARARE = registerSigil("sigil_reparare", "reparare");
    public static final DeferredHolder<Item, Item> SIGIL_TRANSPOSITION = registerSigil("sigil_transposition", "transposition");
    public static final DeferredHolder<Item, Item> SIGIL_FREE_SOUL = registerSigil("sigil_free_soul", "free_soul");
    public static final DeferredHolder<Item, Item> SIGIL_TEMPORAL_DOMINANCE = registerSigil("sigil_temporal_dominance", "temporal_dominance");
    public static final DeferredHolder<Item, Item> SIGIL_EQUIVALENCY = registerSigil("sigil_equivalency", "equivalency");
    public static final DeferredHolder<Item, Item> SIGIL_MONK = registerSigil("sigil_monk", "monk");

    // TODO: ItemSigilBoundlessNature needs to be ported from 1.20.1
    // public static final DeferredHolder<Item, Item> SIGIL_BOUNDLESS_NATURE = registerSigil("sigil_boundless_nature", "boundless_nature");

    public static final DeferredHolder<Item, Item> SPEAR_IRON = ITEMS.registerItem("spear_iron",
        props -> new ItemSpear(ToolMaterial.IRON, props));

    public static final DeferredHolder<Item, Item> SPEAR_DIAMOND = ITEMS.registerItem("spear_diamond",
        props -> new ItemSpear(ToolMaterial.DIAMOND, props));

    public static final DeferredHolder<Item, Item> SPEAR_BOUND = ITEMS.registerItem("spear_bound",
        ItemSpearBound::new);

    public static final DeferredHolder<Item, Item> SPEAR_SENTIENT = ITEMS.registerItem("spear_sentient",
        ItemSpearSentient::new);

    public static final DeferredHolder<Item, Item> SENTIENT_SHIELD = ITEMS.registerItem("sentient_shield",
        ItemSentientShield::new);

    public static final DeferredHolder<Item, Item> SENTIENT_BOW = ITEMS.registerItem("sentient_bow",
        ItemSentientBow::new);

    public static final DeferredHolder<Item, Item> HELLFORGED_BOW = ITEMS.registerItem("hellforged_bow",
        ItemHellforgedBow::new);

    public static final DeferredHolder<Item, Item> RUNIC_SENTIENT_SCYTHE = ITEMS.registerItem("runic_sentient_scythe",
        ItemRunicSentientScythe::new);

    public static final DeferredHolder<Item, Item> HAND_OF_DEATH = ITEMS.registerItem("hand_of_death",
        ItemHandOfDeath::new);

    public static final DeferredHolder<Item, Item> KEY_BINDING = ITEMS.registerItem("key_binding",
        ItemKeyBinding::new);

    public static final DeferredHolder<Item, Item> ACTIVATION_CRYSTAL_FRAGILE = ITEMS.registerItem("activation_crystal_fragile",
        ItemActivationCrystalFragile::new);

    public static final DeferredHolder<Item, Item> SANGUINE_DIVINER = ITEMS.registerItem("sanguine_diviner",
        ItemSanguineDiviner::new);

    public static final DeferredHolder<Item, Item> ANTILIFE_BUCKET = ITEMS.registerItem("antilife_bucket",
        props -> new BucketItem(
            AnimusFluids.ANTILIFE_SOURCE.get(),
            props.craftRemainder(Items.BUCKET).stacksTo(1)
        ));

    public static final DeferredHolder<Item, Item> LIVING_TERRA_BUCKET = ITEMS.registerItem("living_terra_bucket",
        props -> new BucketItem(
            AnimusFluids.LIVING_TERRA_SOURCE.get(),
            props.craftRemainder(Items.BUCKET).stacksTo(1)
        ));
}
