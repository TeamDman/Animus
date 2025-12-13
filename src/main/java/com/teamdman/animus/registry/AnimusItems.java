package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.*;
import com.teamdman.animus.items.sigils.*;
import com.teamdman.animus.items.ItemReagent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;


import java.util.List;

public class AnimusItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(Constants.Mod.MODID);

    /**
     * Helper method to register a BlockItem for a given block
     */
    private static DeferredHolder<Item, Item> registerBlockItem(String name, DeferredHolder<Block, ? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Block Items
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD = registerBlockItem("blood_wood", AnimusBlocks.BLOCK_BLOOD_WOOD);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_STRIPPED = registerBlockItem("blood_wood_stripped", AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_PLANKS = registerBlockItem("blood_wood_planks", AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_STAIRS = registerBlockItem("blood_wood_stairs", AnimusBlocks.BLOCK_BLOOD_WOOD_STAIRS);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_SLAB = registerBlockItem("blood_wood_slab", AnimusBlocks.BLOCK_BLOOD_WOOD_SLAB);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_FENCE = registerBlockItem("blood_wood_fence", AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE);
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_WOOD_FENCE_GATE = registerBlockItem("blood_wood_fence_gate", AnimusBlocks.BLOCK_BLOOD_WOOD_FENCE_GATE);

    // Blood Sapling - with tooltip
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_SAPLING = ITEMS.register("blood_sapling",
        () -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_SAPLING.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_SAPLING_FLAVOUR));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_SAPLING_INFO));
                super.appendHoverText(stack, context, tooltip, flag);
            }
        });

    // Blood Core - with tooltip
    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_CORE = ITEMS.register("blood_core",
        () -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_CORE.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_FLAVOUR));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_INFO));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_MULTIBLOCK));
                super.appendHoverText(stack, context, tooltip, flag);
            }
        });

    public static final DeferredHolder<Item, Item> BLOCK_BLOOD_LEAVES = registerBlockItem("blood_leaves", AnimusBlocks.BLOCK_BLOOD_LEAVES);
    public static final DeferredHolder<Item, Item> BLOCK_ANTILIFE = registerBlockItem("antilife", AnimusBlocks.BLOCK_ANTILIFE);

    // Crystallized Demon Will Block - with tooltip
    public static final DeferredHolder<Item, Item> BLOCK_CRYSTALLIZED_DEMON_WILL = ITEMS.register("crystallized_demon_will_block",
        () -> new BlockItem(AnimusBlocks.BLOCK_CRYSTALLIZED_DEMON_WILL.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_DEMON_WILL_FLAVOUR));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_DEMON_WILL_INFO));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_DEMON_WILL_ALTAR));
                super.appendHoverText(stack, context, tooltip, flag);
            }
        });

    // Imperfect Ritual Stone - with tooltip
    public static final DeferredHolder<Item, Item> BLOCK_IMPERFECT_RITUAL_STONE = ITEMS.register("imperfect_ritual_stone",
        () -> new BlockItem(AnimusBlocks.BLOCK_IMPERFECT_RITUAL_STONE.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.animus.imperfect_ritual_stone.info"));
                super.appendHoverText(stack, context, tooltip, flag);
            }
        });

    // Willful Stone blocks (all 16 colors)
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

    // Regular Items
    public static final DeferredHolder<Item, Item> BLOOD_APPLE = ITEMS.register("blood_apple",
        ItemBloodApple::new);

    public static final DeferredHolder<Item, Item> FRAGMENT_HEALING = ITEMS.register("fragment_healing",
        ItemFragmentHealing::new);

    // Blood Orbs
    public static final DeferredHolder<Item, Item> BLOOD_ORB_TRANSCENDENT = ITEMS.register("blood_orb_transcendent",
        ItemBloodOrbTranscendent::new);

    // Mob Soul - used by Sigil of Chains
    public static final DeferredHolder<Item, Item> MOBSOUL = ITEMS.register("mob_soul",
        com.teamdman.animus.items.ItemMobSoul::new);

    // Reagents - used to craft sigils via Alchemy Arrays
    public static final DeferredHolder<Item, Item> REAGENT_BUILDER = ITEMS.register("reagentbuilder",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_CHAINS = ITEMS.register("reagentchains",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_CONSUMPTION = ITEMS.register("reagentconsumption",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_LEACH = ITEMS.register("reagentleach",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_STORM = ITEMS.register("reagentstorm",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_TRANSPOSITION = ITEMS.register("reagenttransposition",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_BOUNDLESS_NATURE = ITEMS.register("reagentboundlessnature",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_EQUIVALENCY = ITEMS.register("reagentequivalency",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_FREE_SOUL = ITEMS.register("reagentfreesoul",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_HEAVENLY_WRATH = ITEMS.register("reagentheavelywrath",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_REMEDIUM = ITEMS.register("reagentremendium",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_REPARARE = ITEMS.register("reagentreparare",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_TEMPORAL_DOMINANCE = ITEMS.register("reagenttemporaldominance",
        ItemReagent::new);

    public static final DeferredHolder<Item, Item> REAGENT_FIST = ITEMS.register("reagentfist",
        ItemReagent::new);

    // Sigils
    public static final DeferredHolder<Item, Item> SIGIL_BUILDER = ITEMS.register("sigil_builder",
        ItemSigilBuilder::new);

    public static final DeferredHolder<Item, Item> SIGIL_CHAINS = ITEMS.register("sigil_chains",
        ItemSigilChains::new);

    public static final DeferredHolder<Item, Item> SIGIL_CONSUMPTION = ITEMS.register("sigil_consumption",
        ItemSigilConsumption::new);

    public static final DeferredHolder<Item, Item> SIGIL_LEACH = ITEMS.register("sigil_leach",
        ItemSigilLeach::new);

    public static final DeferredHolder<Item, Item> SIGIL_STORM = ITEMS.register("sigil_storm",
        ItemSigilStorm::new);

    public static final DeferredHolder<Item, Item> SIGIL_HEAVENLY_WRATH = ITEMS.register("sigil_heavenly_wrath",
        ItemSigilHeavenlyWrath::new);

    public static final DeferredHolder<Item, Item> SIGIL_REMEDIUM = ITEMS.register("sigil_remedium",
        ItemSigilRemedium::new);

    public static final DeferredHolder<Item, Item> SIGIL_REPARARE = ITEMS.register("sigil_reparare",
        ItemSigilReparare::new);

    public static final DeferredHolder<Item, Item> SIGIL_TRANSPOSITION = ITEMS.register("sigil_transposition",
        ItemSigilTransposition::new);

    public static final DeferredHolder<Item, Item> SIGIL_FREE_SOUL = ITEMS.register("sigil_free_soul",
        ItemSigilFreeSoul::new);

    public static final DeferredHolder<Item, Item> SIGIL_TEMPORAL_DOMINANCE = ITEMS.register("sigil_temporal_dominance",
        ItemSigilTemporalDominance::new);
    public static final DeferredHolder<Item, Item> SIGIL_EQUIVALENCY = ITEMS.register("sigil_equivalency",
        ItemSigilEquivalency::new);

    // TODO: ItemSigilBoundlessNature needs to be ported from 1.20.1
    // public static final DeferredHolder<Item, Item> SIGIL_BOUNDLESS_NATURE = ITEMS.register("sigil_boundless_nature",
    //     ItemSigilBoundlessNature::new);

    public static final DeferredHolder<Item, Item> SIGIL_MONK = ITEMS.register("sigil_monk",
        ItemSigilMonk::new);

    // Tools & Weapons
    public static final DeferredHolder<Item, Item> SPEAR_IRON = ITEMS.register("spear_iron",
        () -> new ItemSpear(Tiers.IRON));

    public static final DeferredHolder<Item, Item> SPEAR_DIAMOND = ITEMS.register("spear_diamond",
        () -> new ItemSpear(Tiers.DIAMOND));

    public static final DeferredHolder<Item, Item> SPEAR_BOUND = ITEMS.register("spear_bound",
        ItemSpearBound::new);

    public static final DeferredHolder<Item, Item> SPEAR_SENTIENT = ITEMS.register("spear_sentient",
        ItemSpearSentient::new);

    public static final DeferredHolder<Item, Item> SENTIENT_SHIELD = ITEMS.register("sentient_shield",
        ItemSentientShield::new);

    // TODO: ItemRunicSentientScythe needs to be ported from 1.20.1
    // public static final DeferredHolder<Item, Item> RUNIC_SENTIENT_SCYTHE = ITEMS.register("runic_sentient_scythe",
    //     ItemRunicSentientScythe::new);

    // TODO: ItemHandOfDeath needs to be ported from 1.20.1
    // public static final DeferredHolder<Item, Item> HAND_OF_DEATH = ITEMS.register("hand_of_death",
    //     ItemHandOfDeath::new);

    // Crafting Components
    public static final DeferredHolder<Item, Item> KEY_BINDING = ITEMS.register("key_binding",
        ItemKeyBinding::new);

    public static final DeferredHolder<Item, Item> ACTIVATION_CRYSTAL_FRAGILE = ITEMS.register("activation_crystal_fragile",
        ItemActivationCrystalFragile::new);

    // Utilities
    public static final DeferredHolder<Item, Item> SANGUINE_DIVINER = ITEMS.register("sanguine_diviner",
        ItemSanguineDiviner::new);

    public static final DeferredHolder<Item, Item> RITUAL_DESIGNER = ITEMS.register("ritual_designer",
        ItemRitualDesigner::new);

    // Fluid Buckets
    public static final DeferredHolder<Item, Item> ANTILIFE_BUCKET = ITEMS.register("antilife_bucket",
        () -> new BucketItem(
            AnimusFluids.ANTILIFE_SOURCE.get(),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
        ));

    public static final DeferredHolder<Item, Item> LIVING_TERRA_BUCKET = ITEMS.register("living_terra_bucket",
        () -> new BucketItem(
            AnimusFluids.LIVING_TERRA_SOURCE.get(),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
        ));
}
