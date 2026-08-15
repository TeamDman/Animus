package com.breakinblocks.animusnv.datagen;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.recipes.KeyUnbindingRecipe;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.fml.ModList;
import com.breakinblocks.neovitae.NeoVitae;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.item.NVItems;

import java.util.concurrent.CompletableFuture;

public class AnimusRecipeProvider extends RecipeProvider {
    private static final ResourceLocation ARRAY_GROWTH = NeoVitae.rl("textures/models/alchemyarrays/growthsigil.png");
    private static final ResourceLocation ARRAY_PHANTOM_BRIDGE = NeoVitae.rl("textures/models/alchemyarrays/phantombridgesigil.png");
    private static final ResourceLocation ARRAY_BINDING = NeoVitae.rl("textures/models/alchemyarrays/bindingarray.png");
    private static final ResourceLocation ARRAY_VOID = NeoVitae.rl("textures/models/alchemyarrays/voidsigil.png");
    private static final ResourceLocation ARRAY_FURNACE = NeoVitae.rl("textures/models/alchemyarrays/furnacearray.png");
    private static final ResourceLocation ARRAY_TELEPOSITION = NeoVitae.rl("textures/models/alchemyarrays/telepositionsigil.png");
    private static final ResourceLocation ARRAY_UPDRAFT = NeoVitae.rl("textures/models/alchemyarrays/updraftarray.png");
    private static final ResourceLocation ARRAY_SIPHON = NeoVitae.rl("textures/models/alchemyarrays/spiritsiphonarray.png");
    private static final ResourceLocation ARRAY_LIGHT = NeoVitae.rl("textures/models/alchemyarrays/bloodlightsigil.png");
    private static final ResourceLocation ARRAY_FASTMINER = NeoVitae.rl("textures/models/alchemyarrays/fastminersigil.png");
    private static final ResourceLocation ARRAY_WATER = NeoVitae.rl("textures/models/alchemyarrays/watersigil.png");
    private static final ResourceLocation ARRAY_MOON = NeoVitae.rl("textures/models/alchemyarrays/moonarray.png");
    private static final ResourceLocation ARRAY_TELEPORTATION = NeoVitae.rl("textures/models/alchemyarrays/teleportation.png");
    private static final ResourceLocation ARRAY_SPIKE = NeoVitae.rl("textures/models/alchemyarrays/spikearray.png");
    private static final ResourceLocation ARRAY_AIR = NeoVitae.rl("textures/models/alchemyarrays/airsigil.png");

    public AnimusRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        buildCraftingRecipes(output);
        buildStonecutterRecipes(output);
        buildAltarRecipes(output);
        buildTabulaVitaeRecipes(output);
        buildHellfireForgeRecipes(output);
        buildArrayRecipes(output);
    }

    private void buildStonecutterRecipes(RecipeOutput output) {
        SingleItemRecipeBuilder.stonecutting(
                Ingredient.of(NVBlocks.CRYSTAL_CLUSTER),
                RecipeCategory.BUILDING_BLOCKS,
                AnimusItems.BLOCK_CRYSTALLIZED_SPIRITUS.get())
            .unlockedBy("has_crystal_cluster", has(NVBlocks.CRYSTAL_CLUSTER))
            .save(output, loc("crystallized_spiritus_block_from_crystal_cluster"));

        SingleItemRecipeBuilder.stonecutting(
                Ingredient.of(AnimusItems.BLOCK_CRYSTALLIZED_SPIRITUS.get()),
                RecipeCategory.BUILDING_BLOCKS,
                NVBlocks.CRYSTAL_CLUSTER)
            .unlockedBy("has_crystallized_spiritus_block", has(AnimusItems.BLOCK_CRYSTALLIZED_SPIRITUS.get()))
            .save(output, loc("crystal_cluster_from_crystallized_spiritus_block"));
    }

    private void buildCraftingRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, AnimusItems.SPEAR_IRON.get())
            .pattern(" a ")
            .pattern("a a")
            .pattern("  b")
            .define('a', Tags.Items.INGOTS_IRON)
            .define('b', Tags.Items.RODS_WOODEN)
            .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, AnimusItems.SPEAR_DIAMOND.get())
            .pattern(" a ")
            .pattern("a a")
            .pattern("  b")
            .define('a', Tags.Items.GEMS_DIAMOND)
            .define('b', Tags.Items.RODS_WOODEN)
            .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get(), 4)
            .requires(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .unlockedBy("has_blood_wood", has(AnimusBlocks.BLOCK_BLOOD_WOOD.get()))
            .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get(), 4)
            .requires(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get())
            .unlockedBy("has_stripped_blood_wood", has(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get()))
            .save(output, loc("blood_wood_planks_from_stripped"));

        SpecialRecipeBuilder.special(KeyUnbindingRecipe::new)
            .save(output, loc("key_binding_unbind").toString());

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, AnimusItems.GUIDE_BOOK.get())
            .requires(Items.BOOK)
            .requires(NVItems.TABULA_RASA.get())
            .unlockedBy("has_tabula_rasa", has(NVItems.TABULA_RASA.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_FENCE.get(), 3)
            .pattern("#S#")
            .pattern("#S#")
            .define('#', AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get())
            .define('S', Items.STICK)
            .unlockedBy("has_blood_wood_planks", has(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_FENCE_GATE.get())
            .pattern("S#S")
            .pattern("S#S")
            .define('#', AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get())
            .define('S', Items.STICK)
            .unlockedBy("has_blood_wood_planks", has(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_SLAB.get(), 6)
            .pattern("###")
            .define('#', AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get())
            .unlockedBy("has_blood_wood_planks", has(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get()))
            .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_STAIRS.get(), 4)
            .pattern("#  ")
            .pattern("## ")
            .pattern("###")
            .define('#', AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get())
            .unlockedBy("has_blood_wood_planks", has(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get()))
            .save(output);

        willfulStoneDyeRecipe(output, Tags.Items.DYES_GRAY, AnimusItems.BLOCK_WILLFUL_STONE.get(), "willful_stone_gray_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_WHITE, AnimusItems.BLOCK_WILLFUL_STONE_WHITE.get(), "willful_stone_white_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_ORANGE, AnimusItems.BLOCK_WILLFUL_STONE_ORANGE.get(), "willful_stone_orange_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_MAGENTA, AnimusItems.BLOCK_WILLFUL_STONE_MAGENTA.get(), "willful_stone_magenta_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_LIGHT_BLUE, AnimusItems.BLOCK_WILLFUL_STONE_LIGHT_BLUE.get(), "willful_stone_light_blue_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_YELLOW, AnimusItems.BLOCK_WILLFUL_STONE_YELLOW.get(), "willful_stone_yellow_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_LIME, AnimusItems.BLOCK_WILLFUL_STONE_LIME.get(), "willful_stone_lime_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_PINK, AnimusItems.BLOCK_WILLFUL_STONE_PINK.get(), "willful_stone_pink_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_LIGHT_GRAY, AnimusItems.BLOCK_WILLFUL_STONE_LIGHT_GRAY.get(), "willful_stone_light_gray_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_CYAN, AnimusItems.BLOCK_WILLFUL_STONE_CYAN.get(), "willful_stone_cyan_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_PURPLE, AnimusItems.BLOCK_WILLFUL_STONE_PURPLE.get(), "willful_stone_purple_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_BLUE, AnimusItems.BLOCK_WILLFUL_STONE_BLUE.get(), "willful_stone_blue_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_BROWN, AnimusItems.BLOCK_WILLFUL_STONE_BROWN.get(), "willful_stone_brown_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_GREEN, AnimusItems.BLOCK_WILLFUL_STONE_GREEN.get(), "willful_stone_green_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_RED, AnimusItems.BLOCK_WILLFUL_STONE_RED.get(), "willful_stone_red_dyeing");
        willfulStoneDyeRecipe(output, Tags.Items.DYES_BLACK, AnimusItems.BLOCK_WILLFUL_STONE_BLACK.get(), "willful_stone_black_dyeing");
    }

    private void willfulStoneDyeRecipe(RecipeOutput output, TagKey<Item> dye, ItemLike result, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 8)
            .pattern("SSS")
            .pattern("SDS")
            .pattern("SSS")
            .define('S', Constants.Tags.WILLFUL_STONES)
            .define('D', dye)
            .unlockedBy("has_willful_stone", has(Constants.Tags.WILLFUL_STONES))
            .save(output, loc(name));
    }

    private void buildAltarRecipes(RecipeOutput output) {
        AltarRecipeBuilder.build(AnimusItems.ACTIVATION_CRYSTAL_FRAGILE.get())
            .from(Items.CLAY_BALL)
            .minTier(2)
            .bloodNeeded(5000)
            .consumption(5)
            .drain(5)
            .save(output, loc("activation_crystal_fragile"));

        AltarRecipeBuilder.build(AnimusItems.BLOOD_ORB_TRANSCENDENT.get())
            .from(AnimusItems.BLOCK_CRYSTALLIZED_SPIRITUS.get())
            .minTier(5)
            .bloodNeeded(80000)
            .consumption(10)
            .drain(10)
            .save(output, loc("blood_orb_transcendent"));

        AltarRecipeBuilder.build(AnimusItems.BLOCK_BLOOD_SAPLING.get())
            .from(ItemTags.SAPLINGS)
            .minTier(0)
            .bloodNeeded(5000)
            .consumption(5)
            .drain(5)
            .save(output, loc("blood_sapling"));

        AltarRecipeBuilder.build(AnimusItems.FRAGMENT_HEALING.get())
            .from(Items.PRISMARINE_SHARD)
            .minTier(1)
            .bloodNeeded(1000)
            .consumption(5)
            .drain(5)
            .save(output, loc("fragment_healing"));

        AltarRecipeBuilder.build(AnimusItems.SANGUINE_DIVINER.get())
            .from(NVItems.RITUAL_DIVINER.get())
            .minTier(1)
            .bloodNeeded(2500)
            .consumption(5)
            .drain(1)
            .save(output, loc("sanguine_diviner"));

        AltarRecipeBuilder.build(AnimusItems.KEY_BINDING.get())
            .from(NVItems.SIMPLE_KEY.get())
            .minTier(1)
            .bloodNeeded(1500)
            .consumption(5)
            .drain(5)
            .save(output, loc("key_binding"));
    }

    private void buildTabulaVitaeRecipes(RecipeOutput output) {
        TabulaVitaeRecipeBuilder.build(AnimusItems.REAGENT_FIST.get())
            .syphon(500)
            .ticks(100)
            .minimumTier(2)
            .input(Items.ROTTEN_FLESH)
            .input(Items.DIAMOND_PICKAXE)
            .input(Items.IRON_SHOVEL)
            .input(Items.IRON_AXE)
            .save(output, loc("alchemytable/reagentfist"));

        TabulaVitaeRecipeBuilder.build(AnimusItems.LIVING_TERRA_BUCKET.get())
            .syphon(1000)
            .ticks(200)
            .minimumTier(2)
            .input(Items.WATER_BUCKET)
            .input(Items.DIRT)
            .input(Items.BONE_MEAL)
            .input(Ingredient.of(ItemTags.SAPLINGS))
            .save(output, loc("alchemytable/living_terra_bucket"));
    }

    private void buildHellfireForgeRecipes(RecipeOutput output) {
        HellfireForgeRecipeBuilder.build(AnimusItems.BLOCK_WILLFUL_STONE.get(), 4)
            .minSpiritus(50.0)
            .drain(25.0)
            .requires(Tags.Items.STONES)
            .requires(Tags.Items.STONES)
            .requires(NVItems.TABULA_ROBUR.get())
            .requires(Tags.Items.DUSTS_REDSTONE)
            .save(output, loc("willful_stone"));

        HellfireForgeRecipeBuilder.build(AnimusItems.SENTIENT_SHIELD.get())
            .minSpiritus(64.0)
            .drain(32.0)
            .requires(Items.SHIELD)
            .requires(NVItems.SPIRITUS_GEM_PETTY.get())
            .requires(Items.DIAMOND)
            .save(output, loc("sentient_shield"));

        HellfireForgeRecipeBuilder.build(AnimusItems.SPEAR_SENTIENT.get())
            .minSpiritus(0.0)
            .drain(0.0)
            .requires(AnimusItems.SPEAR_DIAMOND.get())
            .requires(NVItems.SPIRITUS_GEM_PETTY.get())
            .save(output, loc("spear_sentient"));

        HellfireForgeRecipeBuilder.build(AnimusItems.SENTIENT_BOW.get())
            .minSpiritus(64.0)
            .drain(32.0)
            .requires(Items.BOW)
            .requires(NVItems.SPIRITUS_GEM_PETTY.get())
            .requires(Items.STRING)
            .requires(Items.STRING)
            .save(output, loc("sentient_bow"));

        HellfireForgeRecipeBuilder.build(AnimusItems.HELLFORGED_BOW.get())
            .minSpiritus(256.0)
            .drain(128.0)
            .requires(AnimusItems.SENTIENT_BOW.get())
            .requires(NVItems.SPIRITUS_VINDICTA_CRYSTAL_ITEM.get())
            .requires(Items.BLAZE_ROD)
            .requires(Items.NETHER_STAR)
            .save(output, loc("hellforged_bow"));

        HellfireForgeRecipeBuilder.build(AnimusItems.HAND_OF_DEATH.get())
            .minSpiritus(1024.0)
            .drain(512.0)
            .requires(AnimusItems.RUNIC_SENTIENT_SCYTHE.get())
            .requires(NVItems.SPIRITUS_VINDICTA_CRYSTAL_ITEM.get())
            .requires(Items.WITHER_SKELETON_SKULL)
            .requires(Items.NETHERITE_INGOT)
            .save(output, loc("hand_of_death"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_BOUNDLESS_NATURE.get())
            .minSpiritus(128.0)
            .drain(64.0)
            .requires(Items.OAK_SAPLING)
            .requires(Items.WHEAT_SEEDS)
            .requires(Items.BONE_MEAL)
            .requires(Items.FLOWERING_AZALEA)
            .save(output, loc("reagentboundlessnature"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_BUILDER.get())
            .minSpiritus(128.0)
            .drain(64.0)
            .requires(Items.SUGAR)
            .requires(Items.CRAFTING_TABLE)
            .requires(Items.DISPENSER)
            .requires(Items.BRICKS)
            .save(output, loc("reagentbuilder"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_CHAINS.get())
            .minSpiritus(128.0)
            .drain(64.0)
            .requires(Items.IRON_BARS)
            .requires(Items.ENDER_PEARL)
            .requires(Items.GLASS_BOTTLE)
            .requires(Items.END_STONE)
            .save(output, loc("reagentchains"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_CONSUMPTION.get())
            .minSpiritus(128.0)
            .drain(64.0)
            .requires(Items.IRON_PICKAXE)
            .requires(Items.IRON_PICKAXE)
            .requires(Items.IRON_PICKAXE)
            .requires(Items.IRON_PICKAXE)
            .save(output, loc("reagentconsumption"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_EQUIVALENCY.get())
            .minSpiritus(128.0)
            .drain(64.0)
            .requires(Items.PURPLE_DYE)
            .requires(Items.DIAMOND)
            .requires(Items.EMERALD)
            .requires(Items.QUARTZ_BLOCK)
            .save(output, loc("reagentequivalency"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_FREE_SOUL.get())
            .minSpiritus(256.0)
            .drain(128.0)
            .requires(Items.SOUL_SAND)
            .requires(Items.SOUL_SOIL)
            .requires(Items.PHANTOM_MEMBRANE)
            .requires(Items.TOTEM_OF_UNDYING)
            .save(output, loc("reagentfreesoul"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_HEAVENLY_WRATH.get())
            .minSpiritus(256.0)
            .drain(128.0)
            .requires(Items.FEATHER)
            .requires(Items.PHANTOM_MEMBRANE)
            .requires(Items.SHULKER_SHELL)
            .requires(Items.ANVIL)
            .save(output, loc("reagentheavelywrath"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_LEACH.get())
            .minSpiritus(64.0)
            .drain(32.0)
            .requires(Items.NETHER_WART)
            .requires(Items.CRIMSON_FUNGUS)
            .requires(Ingredient.of(ItemTags.SAPLINGS))
            .requires(Items.VINE)
            .save(output, loc("reagentleach"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_REMEDIUM.get())
            .minSpiritus(128.0)
            .drain(64.0)
            .requires(Items.MILK_BUCKET)
            .requires(Items.GOLDEN_APPLE)
            .requires(Items.GLISTERING_MELON_SLICE)
            .requires(Items.HONEY_BOTTLE)
            .save(output, loc("reagentremendium"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_REPARARE.get())
            .minSpiritus(128.0)
            .drain(64.0)
            .requires(Items.ANVIL)
            .requires(Items.DIAMOND)
            .requires(Items.IRON_INGOT)
            .requires(Items.GRINDSTONE)
            .save(output, loc("reagentreparare"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_STORM.get())
            .minSpiritus(64.0)
            .drain(32.0)
            .requires(Items.SAND)
            .requires(Items.WATER_BUCKET)
            .requires(Items.FISHING_ROD)
            .requires(Items.GHAST_TEAR)
            .save(output, loc("reagentstorm"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_TEMPORAL_DOMINANCE.get())
            .minSpiritus(1024.0)
            .drain(512.0)
            .requires(Items.ECHO_SHARD)
            .requires(Items.CLOCK)
            .requires(Items.NETHERITE_INGOT)
            .requires(Items.CHORUS_FRUIT)
            .save(output, loc("reagenttemporaldominance"));

        HellfireForgeRecipeBuilder.build(AnimusItems.REAGENT_TRANSPOSITION.get())
            .minSpiritus(128.0)
            .drain(64.0)
            .requires(Items.END_STONE)
            .requires(Items.ENDER_PEARL)
            .requires(Items.OBSIDIAN)
            .requires(Items.CHEST)
            .save(output, loc("reagenttransposition"));
    }

    private void buildArrayRecipes(RecipeOutput output) {
        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_BUILDER.get())
            .base(AnimusItems.REAGENT_BUILDER.get())
            .added(NVItems.TABULA_ROBUR.get())
            .texture(ARRAY_PHANTOM_BRIDGE)
            .save(output, loc("array/sigil_builder"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_EQUIVALENCY.get())
            .base(AnimusItems.REAGENT_EQUIVALENCY.get())
            .added(NVItems.TABULA_ROBUR.get())
            .texture(ARRAY_FURNACE)
            .save(output, loc("array/sigil_equivalency"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_LEACH.get())
            .base(AnimusItems.REAGENT_LEACH.get())
            .added(NVItems.TABULA_ROBUR.get())
            .texture(ARRAY_SIPHON)
            .save(output, loc("array/sigil_leach"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_MONK.get())
            .base(AnimusItems.REAGENT_FIST.get())
            .added(NVItems.TABULA_ROBUR.get())
            .texture(ARRAY_FASTMINER)
            .save(output, loc("array/sigil_monk"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_CHAINS.get())
            .base(AnimusItems.REAGENT_CHAINS.get())
            .added(NVItems.TABULA_ANIMATA.get())
            .texture(ARRAY_BINDING)
            .save(output, loc("array/sigil_chains"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_CONSUMPTION.get())
            .base(AnimusItems.REAGENT_CONSUMPTION.get())
            .added(NVItems.TABULA_ANIMATA.get())
            .texture(ARRAY_VOID)
            .save(output, loc("array/sigil_consumption"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_REMEDIUM.get())
            .base(AnimusItems.REAGENT_REMEDIUM.get())
            .added(NVItems.TABULA_ANIMATA.get())
            .texture(ARRAY_LIGHT)
            .save(output, loc("array/sigil_remedium"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_REPARARE.get())
            .base(AnimusItems.REAGENT_REPARARE.get())
            .added(NVItems.TABULA_ANIMATA.get())
            .texture(ARRAY_FASTMINER)
            .save(output, loc("array/sigil_reparare"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_STORM.get())
            .base(AnimusItems.REAGENT_STORM.get())
            .added(NVItems.TABULA_ANIMATA.get())
            .texture(ARRAY_WATER)
            .save(output, loc("array/sigil_storm"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_FREE_SOUL.get())
            .base(AnimusItems.REAGENT_FREE_SOUL.get())
            .added(NVItems.TABULA_SPIRITUS.get())
            .texture(ARRAY_TELEPOSITION)
            .save(output, loc("array/sigil_free_soul"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_HEAVENLY_WRATH.get())
            .base(AnimusItems.REAGENT_HEAVENLY_WRATH.get())
            .added(NVItems.TABULA_SPIRITUS.get())
            .texture(ARRAY_UPDRAFT)
            .save(output, loc("array/sigil_heavenly_wrath"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_TEMPORAL_DOMINANCE.get())
            .base(AnimusItems.REAGENT_TEMPORAL_DOMINANCE.get())
            .added(NVItems.TABULA_SPIRITUS.get())
            .texture(ARRAY_MOON)
            .save(output, loc("array/sigil_temporal_dominance"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_TRANSPOSITION.get())
            .base(AnimusItems.REAGENT_TRANSPOSITION.get())
            .added(NVItems.TABULA_SPIRITUS.get())
            .texture(ARRAY_TELEPORTATION)
            .save(output, loc("array/sigil_transposition"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SPEAR_BOUND.get())
            .base(AnimusItems.SPEAR_DIAMOND.get())
            .added(NVItems.REAGENT_BINDING.get())
            .texture(ARRAY_SPIKE)
            .save(output, loc("array/spear_bound"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.RUNIC_SENTIENT_SCYTHE.get())
            .base(NVItems.SENTIENT_SCYTHE.get())
            .added(NVItems.TABULA_SPIRITUS.get())
            .texture(ARRAY_BINDING)
            .save(output, loc("array/runic_sentient_scythe"));
    }

    private ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, path);
    }
}
