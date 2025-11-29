package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.*;
import com.teamdman.animus.items.sigils.*;
import com.teamdman.animus.items.sigils.ItemSigilTemporalDominance;
import com.teamdman.animus.items.sigils.ItemSigilEquivalency;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class AnimusItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.Mod.MODID);

    /**
     * Helper method to register a BlockItem for a given block
     */
    private static RegistryObject<Item> registerBlockItem(String name, RegistryObject<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Block Items
    public static final RegistryObject<Item> BLOCK_BLOOD_WOOD = registerBlockItem("blood_wood", AnimusBlocks.BLOCK_BLOOD_WOOD);
    public static final RegistryObject<Item> BLOCK_BLOOD_WOOD_STRIPPED = registerBlockItem("blood_wood_stripped", AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED);
    public static final RegistryObject<Item> BLOCK_BLOOD_WOOD_PLANKS = registerBlockItem("blood_wood_planks", AnimusBlocks.BLOCK_BLOOD_WOOD_PLANKS);

    // Blood Sapling - with tooltip
    public static final RegistryObject<Item> BLOCK_BLOOD_SAPLING = ITEMS.register("blood_sapling",
        () -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_SAPLING.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_SAPLING_FLAVOUR));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_SAPLING_INFO));
                super.appendHoverText(stack, level, tooltip, flag);
            }
        });

    // Blood Core - with tooltip
    public static final RegistryObject<Item> BLOCK_BLOOD_CORE = ITEMS.register("blood_core",
        () -> new BlockItem(AnimusBlocks.BLOCK_BLOOD_CORE.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_FLAVOUR));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_INFO));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BLOOD_CORE_MULTIBLOCK));
                super.appendHoverText(stack, level, tooltip, flag);
            }
        });

    public static final RegistryObject<Item> BLOCK_BLOOD_LEAVES = registerBlockItem("blood_leaves", AnimusBlocks.BLOCK_BLOOD_LEAVES);
    public static final RegistryObject<Item> BLOCK_ANTILIFE = registerBlockItem("antilife", AnimusBlocks.BLOCK_ANTILIFE);

    // Crystallized Demon Will Block - with tooltip
    public static final RegistryObject<Item> BLOCK_CRYSTALLIZED_DEMON_WILL = ITEMS.register("crystallized_demon_will_block",
        () -> new BlockItem(AnimusBlocks.BLOCK_CRYSTALLIZED_DEMON_WILL.get(), new Item.Properties()) {
            @Override
            public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_DEMON_WILL_FLAVOUR));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_DEMON_WILL_INFO));
                tooltip.add(Component.translatable(Constants.Localizations.Tooltips.CRYSTALLIZED_DEMON_WILL_ALTAR));
                super.appendHoverText(stack, level, tooltip, flag);
            }
        });

    // Willful Stone blocks (all 16 colors)
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE = registerBlockItem("willful_stone", AnimusBlocks.BLOCK_WILLFUL_STONE);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_WHITE = registerBlockItem("willful_stone_white", AnimusBlocks.BLOCK_WILLFUL_STONE_WHITE);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_ORANGE = registerBlockItem("willful_stone_orange", AnimusBlocks.BLOCK_WILLFUL_STONE_ORANGE);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_MAGENTA = registerBlockItem("willful_stone_magenta", AnimusBlocks.BLOCK_WILLFUL_STONE_MAGENTA);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_LIGHT_BLUE = registerBlockItem("willful_stone_light_blue", AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_BLUE);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_YELLOW = registerBlockItem("willful_stone_yellow", AnimusBlocks.BLOCK_WILLFUL_STONE_YELLOW);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_LIME = registerBlockItem("willful_stone_lime", AnimusBlocks.BLOCK_WILLFUL_STONE_LIME);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_PINK = registerBlockItem("willful_stone_pink", AnimusBlocks.BLOCK_WILLFUL_STONE_PINK);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_LIGHT_GRAY = registerBlockItem("willful_stone_light_gray", AnimusBlocks.BLOCK_WILLFUL_STONE_LIGHT_GRAY);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_CYAN = registerBlockItem("willful_stone_cyan", AnimusBlocks.BLOCK_WILLFUL_STONE_CYAN);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_PURPLE = registerBlockItem("willful_stone_purple", AnimusBlocks.BLOCK_WILLFUL_STONE_PURPLE);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_BLUE = registerBlockItem("willful_stone_blue", AnimusBlocks.BLOCK_WILLFUL_STONE_BLUE);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_BROWN = registerBlockItem("willful_stone_brown", AnimusBlocks.BLOCK_WILLFUL_STONE_BROWN);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_GREEN = registerBlockItem("willful_stone_green", AnimusBlocks.BLOCK_WILLFUL_STONE_GREEN);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_RED = registerBlockItem("willful_stone_red", AnimusBlocks.BLOCK_WILLFUL_STONE_RED);
    public static final RegistryObject<Item> BLOCK_WILLFUL_STONE_BLACK = registerBlockItem("willful_stone_black", AnimusBlocks.BLOCK_WILLFUL_STONE_BLACK);

    // Regular Items
    public static final RegistryObject<Item> BLOOD_APPLE = ITEMS.register("blood_apple",
        ItemBloodApple::new);

    public static final RegistryObject<Item> FRAGMENT_HEALING = ITEMS.register("fragment_healing",
        ItemFragmentHealing::new);

    // Mob Soul - used by Sigil of Chains
    public static final RegistryObject<Item> MOBSOUL = ITEMS.register("mob_soul",
        com.teamdman.animus.items.ItemMobSoul::new);

    // Sigils
    public static final RegistryObject<Item> SIGIL_BUILDER = ITEMS.register("sigil_builder",
        ItemSigilBuilder::new);

    public static final RegistryObject<Item> SIGIL_CHAINS = ITEMS.register("sigil_chains",
        ItemSigilChains::new);

    public static final RegistryObject<Item> SIGIL_CONSUMPTION = ITEMS.register("sigil_consumption",
        ItemSigilConsumption::new);

    public static final RegistryObject<Item> SIGIL_LEACH = ITEMS.register("sigil_leach",
        ItemSigilLeach::new);

    public static final RegistryObject<Item> SIGIL_STORM = ITEMS.register("sigil_storm",
        ItemSigilStorm::new);

    public static final RegistryObject<Item> SIGIL_HEAVENLY_WRATH = ITEMS.register("sigil_heavenly_wrath",
        ItemSigilHeavenlyWrath::new);

    public static final RegistryObject<Item> SIGIL_REMEDIUM = ITEMS.register("sigil_remedium",
        ItemSigilRemedium::new);

    public static final RegistryObject<Item> SIGIL_REPARARE = ITEMS.register("sigil_reparare",
        ItemSigilReparare::new);

    public static final RegistryObject<Item> SIGIL_TRANSPOSITION = ITEMS.register("sigil_transposition",
        ItemSigilTransposition::new);

    public static final RegistryObject<Item> SIGIL_FREE_SOUL = ITEMS.register("sigil_free_soul",
        ItemSigilFreeSoul::new);

    public static final RegistryObject<Item> SIGIL_TEMPORAL_DOMINANCE = ITEMS.register("sigil_temporal_dominance",
        ItemSigilTemporalDominance::new);
    public static final RegistryObject<Item> SIGIL_EQUIVALENCY = ITEMS.register("sigil_equivalency",
        ItemSigilEquivalency::new);

    // Tools & Weapons
    public static final RegistryObject<Item> PILUM_IRON = ITEMS.register("pilum_iron",
        () -> new ItemPilum(Tiers.IRON));

    public static final RegistryObject<Item> PILUM_DIAMOND = ITEMS.register("pilum_diamond",
        () -> new ItemPilum(Tiers.DIAMOND));

    public static final RegistryObject<Item> PILUM_BOUND = ITEMS.register("pilum_bound",
        ItemPilumBound::new);

    public static final RegistryObject<Item> PILUM_SENTIENT = ITEMS.register("pilum_sentient",
        ItemPilumSentient::new);

    // Crafting Components
    public static final RegistryObject<Item> KEY_BINDING = ITEMS.register("key_binding",
        ItemKeyBinding::new);

    public static final RegistryObject<Item> ACTIVATION_CRYSTAL_FRAGILE = ITEMS.register("activation_crystal_fragile",
        ItemActivationCrystalFragile::new);

    // Utilities
    public static final RegistryObject<Item> SANGUINE_DIVINER = ITEMS.register("sanguine_diviner",
        ItemSanguineDiviner::new);

    // Fluid Buckets
    public static final RegistryObject<Item> ANTILIFE_BUCKET = ITEMS.register("antilife_bucket",
        () -> new BucketItem(
            AnimusFluids.ANTILIFE_SOURCE,
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
        ));

    public static final RegistryObject<Item> LIVING_TERRA_BUCKET = ITEMS.register("living_terra_bucket",
        () -> new BucketItem(
            AnimusFluids.LIVING_TERRA_SOURCE,
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
        ));
}
