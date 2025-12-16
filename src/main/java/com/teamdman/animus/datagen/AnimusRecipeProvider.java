package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.recipes.KeyUnbindingRecipe;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.item.BMItems;

import java.util.concurrent.CompletableFuture;

public class AnimusRecipeProvider extends RecipeProvider {
    // Common array textures
    private static final ResourceLocation ARRAY_GROWTH = BloodMagic.rl("textures/models/alchemyarrays/growthsigil.png");
    private static final ResourceLocation ARRAY_LAPUTA = BloodMagic.rl("textures/models/alchemyarrays/shardoflaputa.png");
    private static final ResourceLocation ARRAY_BINDING = BloodMagic.rl("textures/models/alchemyarrays/bindingarray.png");
    private static final ResourceLocation ARRAY_VOID = BloodMagic.rl("textures/models/alchemyarrays/voidsigil.png");
    private static final ResourceLocation ARRAY_FURNACE = BloodMagic.rl("textures/models/alchemyarrays/furnacearray.png");
    private static final ResourceLocation ARRAY_TELEPORT = BloodMagic.rl("textures/models/alchemyarrays/teleportationarray.png");
    private static final ResourceLocation ARRAY_LIGHTNING = BloodMagic.rl("textures/models/alchemyarrays/bindinglightningarray.png");
    private static final ResourceLocation ARRAY_MOBSACRIFICE = BloodMagic.rl("textures/models/alchemyarrays/mobsacrifice.png");
    private static final ResourceLocation ARRAY_LIGHT = BloodMagic.rl("textures/models/alchemyarrays/lightsigil.png");
    private static final ResourceLocation ARRAY_FASTMINER = BloodMagic.rl("textures/models/alchemyarrays/fastminersigil.png");
    private static final ResourceLocation ARRAY_WATER = BloodMagic.rl("textures/models/alchemyarrays/watersigil.png");
    private static final ResourceLocation ARRAY_MOON = BloodMagic.rl("textures/models/alchemyarrays/moonarray.png");
    private static final ResourceLocation ARRAY_TELEPORTATION = BloodMagic.rl("textures/models/alchemyarrays/teleportation.png");
    private static final ResourceLocation ARRAY_SPIKE = BloodMagic.rl("textures/models/alchemyarrays/spikearray.png");
    private static final ResourceLocation ARRAY_AIR = BloodMagic.rl("textures/models/alchemyarrays/airsigil.png");

    public AnimusRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        buildCraftingRecipes(output);
        buildAltarRecipes(output);
        buildAlchemyTableRecipes(output);
        buildSoulForgeRecipes(output);
        buildArrayRecipes(output);
    }

    private void buildCraftingRecipes(RecipeOutput output) {
        // Spears
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

        // Blood Apple - no crafting recipe, obtained from Blood Leaves loot table

        // Blood Wood Planks from Blood Wood
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get(), 4)
            .requires(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .unlockedBy("has_blood_wood", has(AnimusBlocks.BLOCK_BLOOD_WOOD.get()))
            .save(output);

        // Blood Wood Planks from Stripped Blood Wood
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get(), 4)
            .requires(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get())
            .unlockedBy("has_stripped_blood_wood", has(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get()))
            .save(output, loc("blood_wood_planks_from_stripped"));

        // Key Binding unbind special recipe
        SpecialRecipeBuilder.special(KeyUnbindingRecipe::new)
            .save(output, loc("key_binding_unbind").toString());

        // Blood wood building blocks
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

        // Willful stone dyeing recipes - shaped 8 around dye
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

    private void willfulStoneDyeRecipe(RecipeOutput output, net.minecraft.tags.TagKey<net.minecraft.world.item.Item> dye, ItemLike result, String name) {
        // 8 willful stones surrounding a dye = 8 colored willful stones
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
        // Fragile Activation Crystal - clay ball on T2 altar
        AltarRecipeBuilder.build(AnimusItems.ACTIVATION_CRYSTAL_FRAGILE.get())
            .from(Items.CLAY_BALL)
            .minTier(2)
            .bloodNeeded(5000)
            .consumption(5)
            .drain(5)
            .save(output, loc("activation_crystal_fragile"));

        // Transcendent Blood Orb - crystallized demon will on T6 altar (tier index 5 = tier 6)
        AltarRecipeBuilder.build(AnimusItems.BLOOD_ORB_TRANSCENDENT.get())
            .from(AnimusItems.BLOCK_CRYSTALLIZED_DEMON_WILL.get())
            .minTier(5)
            .bloodNeeded(80000)
            .consumption(10)
            .drain(10)
            .save(output, loc("blood_orb_transcendent"));

        // Blood Sapling - any sapling on T0 altar
        AltarRecipeBuilder.build(AnimusItems.BLOCK_BLOOD_SAPLING.get())
            .from(ItemTags.SAPLINGS)
            .minTier(0)
            .bloodNeeded(5000)
            .consumption(5)
            .drain(5)
            .save(output, loc("blood_sapling"));

        // Healing Fragment - prismarine shard on T1 altar
        AltarRecipeBuilder.build(AnimusItems.FRAGMENT_HEALING.get())
            .from(Items.PRISMARINE_SHARD)
            .minTier(1)
            .bloodNeeded(1000)
            .consumption(5)
            .drain(5)
            .save(output, loc("fragment_healing"));

        // Sanguine Diviner - ritual diviner on T1 altar
        AltarRecipeBuilder.build(AnimusItems.SANGUINE_DIVINER.get())
            .from(BMItems.RITUAL_DIVINER.get())
            .minTier(1)
            .bloodNeeded(2500)
            .consumption(5)
            .drain(1)
            .save(output, loc("sanguine_diviner"));

        // Key of Binding - simple key on T1 altar
        AltarRecipeBuilder.build(AnimusItems.KEY_BINDING.get())
            .from(BMItems.SIMPLE_KEY.get())
            .minTier(1)
            .bloodNeeded(1500)
            .consumption(5)
            .drain(5)
            .save(output, loc("key_binding"));
    }

    private void buildAlchemyTableRecipes(RecipeOutput output) {
        // Reagent of the Fist - crafted in Alchemy Table
        AlchemyTableRecipeBuilder.build(AnimusItems.REAGENT_FIST.get())
            .syphon(500)
            .ticks(100)
            .minimumTier(2)
            .input(Items.ROTTEN_FLESH)
            .input(Items.DIAMOND_PICKAXE)
            .input(Items.IRON_SHOVEL)
            .input(Items.IRON_AXE)
            .save(output, loc("alchemytable/reagentfist"));

        // Living Terra Bucket - earth-infused fluid that solidifies into terrain
        AlchemyTableRecipeBuilder.build(AnimusItems.LIVING_TERRA_BUCKET.get())
            .syphon(1000)
            .ticks(200)
            .minimumTier(2)
            .input(Items.WATER_BUCKET)
            .input(Items.DIRT)
            .input(Items.BONE_MEAL)
            .input(Ingredient.of(ItemTags.SAPLINGS))
            .save(output, loc("alchemytable/living_terra_bucket"));
    }

    private void buildSoulForgeRecipes(RecipeOutput output) {
        // Crystallized Demon Will Block
        SoulForgeRecipeBuilder.build(AnimusItems.BLOCK_CRYSTALLIZED_DEMON_WILL.get())
            .minWill(2048.0)
            .drain(1024.0)
            .requires(Items.SCULK)
            .requires(BMItems.SLATE_ETHEREAL.get())
            .requires(BMItems.WEAK_BLOOD_SHARD.get())
            .requires(Items.NETHER_STAR)
            .save(output, loc("crystallized_demon_will_block"));

        // Sentient Shield
        SoulForgeRecipeBuilder.build(AnimusItems.SENTIENT_SHIELD.get())
            .minWill(200.0)
            .drain(100.0)
            .requires(Items.SHIELD)
            .requires(BMItems.SOUL_GEM_PETTY.get())
            .requires(Items.DIAMOND)
            .save(output, loc("sentient_shield"));

        // Sentient Spear
        SoulForgeRecipeBuilder.build(AnimusItems.SPEAR_SENTIENT.get())
            .minWill(0.0)
            .drain(0.0)
            .requires(AnimusItems.SPEAR_DIAMOND.get())
            .requires(BMItems.SOUL_GEM_PETTY.get())
            .save(output, loc("spear_sentient"));

        // Hand of Death - ultimate scythe, requires high demon will
        SoulForgeRecipeBuilder.build(AnimusItems.HAND_OF_DEATH.get())
            .minWill(1024.0)
            .drain(512.0)
            .requires(AnimusItems.RUNIC_SENTIENT_SCYTHE.get())
            .requires(BMItems.VENGEFUL_CRYSTAL.get())
            .requires(Items.WITHER_SKELETON_SKULL)
            .requires(Items.NETHERITE_INGOT)
            .save(output, loc("hand_of_death"));

        // Reagents
        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_BOUNDLESS_NATURE.get())
            .minWill(128.0)
            .drain(64.0)
            .requires(Items.OAK_SAPLING)
            .requires(Items.WHEAT_SEEDS)
            .requires(Items.BONE_MEAL)
            .requires(Items.FLOWERING_AZALEA)
            .save(output, loc("reagentboundlessnature"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_BUILDER.get())
            .minWill(128.0)
            .drain(64.0)
            .requires(Items.SUGAR)
            .requires(Items.CRAFTING_TABLE)
            .requires(Items.DISPENSER)
            .requires(Items.BRICKS)
            .save(output, loc("reagentbuilder"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_CHAINS.get())
            .minWill(128.0)
            .drain(64.0)
            .requires(Items.IRON_BARS)
            .requires(Items.ENDER_PEARL)
            .requires(Items.GLASS_BOTTLE)
            .requires(Items.END_STONE)
            .save(output, loc("reagentchains"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_CONSUMPTION.get())
            .minWill(128.0)
            .drain(64.0)
            .requires(Items.IRON_PICKAXE)
            .requires(Items.IRON_PICKAXE)
            .requires(Items.IRON_PICKAXE)
            .requires(Items.IRON_PICKAXE)
            .save(output, loc("reagentconsumption"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_EQUIVALENCY.get())
            .minWill(128.0)
            .drain(64.0)
            .requires(Items.PURPLE_DYE)
            .requires(Items.DIAMOND)
            .requires(Items.EMERALD)
            .requires(Items.QUARTZ_BLOCK)
            .save(output, loc("reagentequivalency"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_FREE_SOUL.get())
            .minWill(256.0)
            .drain(128.0)
            .requires(Items.SOUL_SAND)
            .requires(Items.SOUL_SOIL)
            .requires(Items.PHANTOM_MEMBRANE)
            .requires(Items.TOTEM_OF_UNDYING)
            .save(output, loc("reagentfreesoul"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_HEAVENLY_WRATH.get())
            .minWill(256.0)
            .drain(128.0)
            .requires(Items.FEATHER)
            .requires(Items.PHANTOM_MEMBRANE)
            .requires(Items.SHULKER_SHELL)
            .requires(Items.ANVIL)
            .save(output, loc("reagentheavelywrath"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_LEACH.get())
            .minWill(64.0)
            .drain(32.0)
            .requires(Items.NETHER_WART)
            .requires(Items.CRIMSON_FUNGUS)
            .requires(Ingredient.of(ItemTags.SAPLINGS))
            .requires(Items.VINE)
            .save(output, loc("reagentleach"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_REMEDIUM.get())
            .minWill(128.0)
            .drain(64.0)
            .requires(Items.MILK_BUCKET)
            .requires(Items.GOLDEN_APPLE)
            .requires(Items.GLISTERING_MELON_SLICE)
            .requires(Items.HONEY_BOTTLE)
            .save(output, loc("reagentremendium"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_REPARARE.get())
            .minWill(128.0)
            .drain(64.0)
            .requires(Items.ANVIL)
            .requires(Items.DIAMOND)
            .requires(Items.IRON_INGOT)
            .requires(Items.GRINDSTONE)
            .save(output, loc("reagentreparare"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_STORM.get())
            .minWill(64.0)
            .drain(32.0)
            .requires(Items.SAND)
            .requires(Items.WATER_BUCKET)
            .requires(Items.FISHING_ROD)
            .requires(Items.GHAST_TEAR)
            .save(output, loc("reagentstorm"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_TEMPORAL_DOMINANCE.get())
            .minWill(1024.0)
            .drain(512.0)
            .requires(Items.ECHO_SHARD)
            .requires(Items.CLOCK)
            .requires(Items.NETHERITE_INGOT)
            .requires(Items.CHORUS_FRUIT)
            .save(output, loc("reagenttemporaldominance"));

        SoulForgeRecipeBuilder.build(AnimusItems.REAGENT_TRANSPOSITION.get())
            .minWill(128.0)
            .drain(64.0)
            .requires(Items.END_STONE)
            .requires(Items.ENDER_PEARL)
            .requires(Items.OBSIDIAN)
            .requires(Items.CHEST)
            .save(output, loc("reagenttransposition"));
    }

    private void buildArrayRecipes(RecipeOutput output) {
        // Sigils with reinforced slate (tier 2)
        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_BUILDER.get())
            .base(AnimusItems.REAGENT_BUILDER.get())
            .added(BMItems.SLATE_REINFORCED.get())
            .texture(ARRAY_LAPUTA)
            .save(output, loc("array/sigil_builder"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_EQUIVALENCY.get())
            .base(AnimusItems.REAGENT_EQUIVALENCY.get())
            .added(BMItems.SLATE_REINFORCED.get())
            .texture(ARRAY_FURNACE)
            .save(output, loc("array/sigil_equivalency"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_LEACH.get())
            .base(AnimusItems.REAGENT_LEACH.get())
            .added(BMItems.SLATE_REINFORCED.get())
            .texture(ARRAY_MOBSACRIFICE)
            .save(output, loc("array/sigil_leach"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_MONK.get())
            .base(AnimusItems.REAGENT_FIST.get())
            .added(BMItems.SLATE_REINFORCED.get())
            .texture(ARRAY_FASTMINER)
            .save(output, loc("array/sigil_monk"));

        // Sigils with imbued slate (tier 3)
        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_CHAINS.get())
            .base(AnimusItems.REAGENT_CHAINS.get())
            .added(BMItems.SLATE_IMBUED.get())
            .texture(ARRAY_BINDING)
            .save(output, loc("array/sigil_chains"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_CONSUMPTION.get())
            .base(AnimusItems.REAGENT_CONSUMPTION.get())
            .added(BMItems.SLATE_IMBUED.get())
            .texture(ARRAY_VOID)
            .save(output, loc("array/sigil_consumption"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_REMEDIUM.get())
            .base(AnimusItems.REAGENT_REMEDIUM.get())
            .added(BMItems.SLATE_IMBUED.get())
            .texture(ARRAY_LIGHT)
            .save(output, loc("array/sigil_remedium"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_REPARARE.get())
            .base(AnimusItems.REAGENT_REPARARE.get())
            .added(BMItems.SLATE_IMBUED.get())
            .texture(ARRAY_FASTMINER)
            .save(output, loc("array/sigil_reparare"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_STORM.get())
            .base(AnimusItems.REAGENT_STORM.get())
            .added(BMItems.SLATE_IMBUED.get())
            .texture(ARRAY_WATER)
            .save(output, loc("array/sigil_storm"));

        // Sigils with demonic slate (tier 4)
        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_FREE_SOUL.get())
            .base(AnimusItems.REAGENT_FREE_SOUL.get())
            .added(BMItems.SLATE_DEMONIC.get())
            .texture(ARRAY_TELEPORT)
            .save(output, loc("array/sigil_free_soul"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_HEAVENLY_WRATH.get())
            .base(AnimusItems.REAGENT_HEAVENLY_WRATH.get())
            .added(BMItems.SLATE_DEMONIC.get())
            .texture(ARRAY_LIGHTNING)
            .save(output, loc("array/sigil_heavenly_wrath"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_TEMPORAL_DOMINANCE.get())
            .base(AnimusItems.REAGENT_TEMPORAL_DOMINANCE.get())
            .added(BMItems.SLATE_DEMONIC.get())
            .texture(ARRAY_MOON)
            .save(output, loc("array/sigil_temporal_dominance"));

        AlchemyArrayRecipeBuilder.build(AnimusItems.SIGIL_TRANSPOSITION.get())
            .base(AnimusItems.REAGENT_TRANSPOSITION.get())
            .added(BMItems.SLATE_DEMONIC.get())
            .texture(ARRAY_TELEPORTATION)
            .save(output, loc("array/sigil_transposition"));

        // Other array recipes
        AlchemyArrayRecipeBuilder.build(AnimusItems.SPEAR_BOUND.get())
            .base(AnimusItems.SPEAR_DIAMOND.get())
            .added(BMItems.REAGENT_BINDING.get())
            .texture(ARRAY_SPIKE)
            .save(output, loc("array/spear_bound"));

        // Runic Sentient Scythe - created by infusing sentient scythe with a bound slate
        AlchemyArrayRecipeBuilder.build(AnimusItems.RUNIC_SENTIENT_SCYTHE.get())
            .base(BMItems.SENTIENT_SCYTHE.get())
            .added(BMItems.SLATE_DEMONIC.get())
            .texture(ARRAY_BINDING)
            .save(output, loc("array/runic_sentient_scythe"));
    }

    private ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, path);
    }
}
