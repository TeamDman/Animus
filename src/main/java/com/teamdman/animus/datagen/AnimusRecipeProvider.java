package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.IronsSpellsCompat;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusRecipeSerializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import wayoftime.bloodmagic.common.data.recipe.builder.AlchemyTableRecipeBuilder;
import wayoftime.bloodmagic.common.data.recipe.builder.BloodAltarRecipeBuilder;
import wayoftime.bloodmagic.common.data.recipe.builder.TartaricForgeRecipeBuilder;

import java.util.function.Consumer;

public class AnimusRecipeProvider extends RecipeProvider implements IConditionBuilder {
    // Common array textures
    private static final String ARRAY_GROWTH = "bloodmagic:textures/models/alchemyarrays/growthsigil.png";
    private static final String ARRAY_LAPUTA = "bloodmagic:textures/models/alchemyarrays/shardoflaputa.png";
    private static final String ARRAY_BINDING = "bloodmagic:textures/models/alchemyarrays/bindingarray.png";
    private static final String ARRAY_VOID = "bloodmagic:textures/models/alchemyarrays/voidsigil.png";
    private static final String ARRAY_FURNACE = "bloodmagic:textures/models/alchemyarrays/furnacearray.png";
    private static final String ARRAY_TELEPORT = "bloodmagic:textures/models/alchemyarrays/teleportationarray.png";
    private static final String ARRAY_LIGHTNING = "bloodmagic:textures/models/alchemyarrays/bindinglightningarray.png";
    private static final String ARRAY_MOBSACRIFICE = "bloodmagic:textures/models/alchemyarrays/mobsacrifice.png";
    private static final String ARRAY_LIGHT = "bloodmagic:textures/models/alchemyarrays/lightsigil.png";
    private static final String ARRAY_FASTMINER = "bloodmagic:textures/models/alchemyarrays/fastminersigil.png";
    private static final String ARRAY_WATER = "bloodmagic:textures/models/alchemyarrays/watersigil.png";
    private static final String ARRAY_MOON = "bloodmagic:textures/models/alchemyarrays/moonarray.png";
    private static final String ARRAY_TELEPORTATION = "bloodmagic:textures/models/alchemyarrays/teleportation.png";
    private static final String ARRAY_SPIKE = "bloodmagic:textures/models/alchemyarrays/spikearray.png";
    private static final String ARRAY_AIR = "bloodmagic:textures/models/alchemyarrays/airsigil.png";

    public AnimusRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        buildCraftingRecipes(consumer);
        buildAltarRecipes(consumer);
        buildAlchemyTableRecipes(consumer);
        buildSoulForgeRecipes(consumer);
        buildArrayRecipes(consumer);
    }

    private void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, AnimusItems.SPEAR_IRON.get())
            .pattern(" a ")
            .pattern("a a")
            .pattern("  b")
            .define('a', Tags.Items.INGOTS_IRON)
            .define('b', Tags.Items.RODS_WOODEN)
            .unlockedBy("has_iron", has(Tags.Items.INGOTS_IRON))
            .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, AnimusItems.SPEAR_DIAMOND.get())
            .pattern(" a ")
            .pattern("a a")
            .pattern("  b")
            .define('a', Tags.Items.GEMS_DIAMOND)
            .define('b', Tags.Items.RODS_WOODEN)
            .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
            .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, AnimusItems.BLOOD_APPLE.get())
            .requires(Items.APPLE)
            .requires(Items.REDSTONE)
            .requires(Items.REDSTONE)
            .unlockedBy("has_apple", has(Items.APPLE))
            .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get(), 4)
            .requires(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .unlockedBy("has_blood_wood", has(AnimusBlocks.BLOCK_BLOOD_WOOD.get()))
            .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get(), 4)
            .requires(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get())
            .unlockedBy("has_stripped_blood_wood", has(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get()))
            .save(consumer, ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "blood_wood_planks_from_stripped"));

        SpecialRecipeBuilder.special(AnimusRecipeSerializers.KEY_UNBINDING.get())
            .save(consumer, ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "key_binding_unbind").toString());

        // Blood wood building blocks
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_FENCE.get(), 3)
            .pattern("#S#")
            .pattern("#S#")
            .define('#', AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get())
            .define('S', Items.STICK)
            .unlockedBy("has_blood_wood_planks", has(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get()))
            .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_FENCE_GATE.get())
            .pattern("S#S")
            .pattern("S#S")
            .define('#', AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get())
            .define('S', Items.STICK)
            .unlockedBy("has_blood_wood_planks", has(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get()))
            .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_SLAB.get(), 6)
            .pattern("###")
            .define('#', AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get())
            .unlockedBy("has_blood_wood_planks", has(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get()))
            .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_STAIRS.get(), 4)
            .pattern("#  ")
            .pattern("## ")
            .pattern("###")
            .define('#', AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get())
            .unlockedBy("has_blood_wood_planks", has(AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get()))
            .save(consumer);

        // Imperfect ritual stone
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AnimusItems.BLOCK_IMPERFECT_RITUAL_STONE.get())
            .pattern("OSO")
            .pattern("SBS")
            .pattern("OSO")
            .define('O', Items.OBSIDIAN)
            .define('S', Tags.Items.STONE)
            .define('B', itemFromMod("bloodmagic", "apprenticebloodorb"))
            .unlockedBy("has_blood_orb", has(itemFromMod("bloodmagic", "apprenticebloodorb")))
            .save(consumer);

        // Willful stone dyeing recipes
        willfulStoneDyeRecipe(consumer, Items.GRAY_DYE, AnimusItems.BLOCK_WILLFUL_STONE.get(), "willful_stone_dyeing");
        willfulStoneDyeRecipe(consumer, Items.WHITE_DYE, AnimusItems.BLOCK_WILLFUL_STONE_WHITE.get(), "willful_stone_white_dyeing");
        willfulStoneDyeRecipe(consumer, Items.ORANGE_DYE, AnimusItems.BLOCK_WILLFUL_STONE_ORANGE.get(), "willful_stone_orange_dyeing");
        willfulStoneDyeRecipe(consumer, Items.MAGENTA_DYE, AnimusItems.BLOCK_WILLFUL_STONE_MAGENTA.get(), "willful_stone_magenta_dyeing");
        willfulStoneDyeRecipe(consumer, Items.LIGHT_BLUE_DYE, AnimusItems.BLOCK_WILLFUL_STONE_LIGHT_BLUE.get(), "willful_stone_light_blue_dyeing");
        willfulStoneDyeRecipe(consumer, Items.YELLOW_DYE, AnimusItems.BLOCK_WILLFUL_STONE_YELLOW.get(), "willful_stone_yellow_dyeing");
        willfulStoneDyeRecipe(consumer, Items.LIME_DYE, AnimusItems.BLOCK_WILLFUL_STONE_LIME.get(), "willful_stone_lime_dyeing");
        willfulStoneDyeRecipe(consumer, Items.PINK_DYE, AnimusItems.BLOCK_WILLFUL_STONE_PINK.get(), "willful_stone_pink_dyeing");
        willfulStoneDyeRecipe(consumer, Items.LIGHT_GRAY_DYE, AnimusItems.BLOCK_WILLFUL_STONE_LIGHT_GRAY.get(), "willful_stone_light_gray_dyeing");
        willfulStoneDyeRecipe(consumer, Items.CYAN_DYE, AnimusItems.BLOCK_WILLFUL_STONE_CYAN.get(), "willful_stone_cyan_dyeing");
        willfulStoneDyeRecipe(consumer, Items.PURPLE_DYE, AnimusItems.BLOCK_WILLFUL_STONE_PURPLE.get(), "willful_stone_purple_dyeing");
        willfulStoneDyeRecipe(consumer, Items.BLUE_DYE, AnimusItems.BLOCK_WILLFUL_STONE_BLUE.get(), "willful_stone_blue_dyeing");
        willfulStoneDyeRecipe(consumer, Items.BROWN_DYE, AnimusItems.BLOCK_WILLFUL_STONE_BROWN.get(), "willful_stone_brown_dyeing");
        willfulStoneDyeRecipe(consumer, Items.GREEN_DYE, AnimusItems.BLOCK_WILLFUL_STONE_GREEN.get(), "willful_stone_green_dyeing");
        willfulStoneDyeRecipe(consumer, Items.RED_DYE, AnimusItems.BLOCK_WILLFUL_STONE_RED.get(), "willful_stone_red_dyeing");
        willfulStoneDyeRecipe(consumer, Items.BLACK_DYE, AnimusItems.BLOCK_WILLFUL_STONE_BLACK.get(), "willful_stone_black_dyeing");
    }

    private void willfulStoneDyeRecipe(Consumer<FinishedRecipe> consumer, ItemLike dye, ItemLike result, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, result, 8)
            .pattern("SSS")
            .pattern("SDS")
            .pattern("SSS")
            .define('S', Constants.Tags.WILLFUL_STONES)
            .define('D', dye)
            .unlockedBy("has_willful_stone", has(Constants.Tags.WILLFUL_STONES))
            .save(consumer, loc(name));
    }

    private void buildAltarRecipes(Consumer<FinishedRecipe> consumer) {
        BloodAltarRecipeBuilder.altar(
                Ingredient.of(Items.CLAY_BALL),
                new ItemStack(AnimusItems.ACTIVATION_CRYSTAL_FRAGILE.get()),
                2, 5000, 5, 5)
            .build(consumer, loc("altar/activation_crystal_fragile"));

        BloodAltarRecipeBuilder.altar(
                Ingredient.of(AnimusItems.BLOCK_CRYSTALLIZED_DEMON_WILL.get()),
                new ItemStack(AnimusItems.BLOOD_ORB_TRANSCENDENT.get()),
                6, 80000, 10, 10)
            .build(consumer, loc("altar/blood_orb_transcendent"));

        BloodAltarRecipeBuilder.altar(
                Ingredient.of(ItemTags.SAPLINGS),
                new ItemStack(AnimusItems.BLOCK_BLOOD_SAPLING.get()),
                0, 5000, 5, 5)
            .build(consumer, loc("altar/blood_sapling"));

        // Diabolical Fungi recipe - uses Botania compat item
        BloodAltarRecipeBuilder.altar(
                Ingredient.of(itemFromMod("botania", "endoflame")),
                new ItemStack(itemFromMod("animus", "diabolical_fungi")),
                2, 2000, 5, 1)
            .build(consumer, loc("altar/diabolical_fungi"));

        BloodAltarRecipeBuilder.altar(
                Ingredient.of(Items.PRISMARINE_SHARD),
                new ItemStack(AnimusItems.FRAGMENT_HEALING.get()),
                1, 1000, 5, 5)
            .build(consumer, loc("altar/fragment_healing"));

        BloodAltarRecipeBuilder.altar(
                Ingredient.of(itemFromMod("bloodmagic", "ritualdiviner")),
                new ItemStack(AnimusItems.SANGUINE_DIVINER.get()),
                1, 2500, 5, 1)
            .build(consumer, loc("altar/sanguine_diviner"));

        BloodAltarRecipeBuilder.altar(
                Ingredient.of(itemFromMod("bloodmagic", "simplekey")),
                new ItemStack(AnimusItems.KEY_BINDING.get()),
                1, 1500, 5, 5)
            .build(consumer, loc("altar/key_binding"));
    }

    private void buildAlchemyTableRecipes(Consumer<FinishedRecipe> consumer) {
        // Reagent of the Fist - crafted in Alchemy Table with zombie flesh + diamond pickaxe + iron tools
        // Requires T2 blood orb (apprentice), 500 LP, 100 ticks
        AlchemyTableRecipeBuilder.alchemyTable(
                new ItemStack(AnimusItems.REAGENT_FIST.get()),
                500, // LP cost
                100, // ticks
                2    // minimum tier (apprentice blood orb)
            )
            .addIngredient(Ingredient.of(Items.ROTTEN_FLESH))
            .addIngredient(Ingredient.of(Items.DIAMOND_PICKAXE))
            .addIngredient(Ingredient.of(Items.IRON_SHOVEL))
            .addIngredient(Ingredient.of(Items.IRON_AXE))
            .build(consumer, loc("alchemytable/reagentfist"));
    }

    private void buildSoulForgeRecipes(Consumer<FinishedRecipe> consumer) {
        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.BLOCK_CRYSTALLIZED_DEMON_WILL.get()),
                2048.0, 1024.0,
                Ingredient.of(Items.SCULK),
                Ingredient.of(itemFromMod("bloodmagic", "etherealslate")),
                Ingredient.of(itemFromMod("bloodmagic", "weakbloodshard")),
                Ingredient.of(Items.NETHER_STAR))
            .build(consumer, loc("soulforge/crystallized_demon_will_block"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.HAND_OF_DEATH.get()),
                2048.0, 1024.0,
                Ingredient.of(AnimusItems.RUNIC_SENTIENT_SCYTHE.get()),
                Ingredient.of(itemFromMod("bloodmagic", "demonslate")),
                Ingredient.of(Items.DRAGON_BREATH),
                Ingredient.of(itemFromMod("malum", "rune_of_culling")))
            .build(consumer, loc("soulforge/hand_of_death"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.SENTIENT_SHIELD.get()),
                200.0, 100.0,
                Ingredient.of(Items.SHIELD),
                Ingredient.of(itemFromMod("bloodmagic", "soulgempetty")),
                Ingredient.of(Items.DIAMOND))
            .build(consumer, loc("soulforge/sentient_shield"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.SPEAR_SENTIENT.get()),
                0.0, 0.0,
                Ingredient.of(AnimusItems.SPEAR_DIAMOND.get()),
                Ingredient.of(itemFromMod("bloodmagic", "soulgempetty")))
            .build(consumer, loc("soulforge/spear_sentient"));

        // Reagents
        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_BOUNDLESS_NATURE.get()),
                128.0, 64.0,
                Ingredient.of(Items.OAK_SAPLING),
                Ingredient.of(Items.WHEAT_SEEDS),
                Ingredient.of(Items.BONE_MEAL),
                Ingredient.of(Items.FLOWERING_AZALEA))
            .build(consumer, loc("soulforge/reagentboundlessnature"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_BUILDER.get()),
                128.0, 64.0,
                Ingredient.of(Items.SUGAR),
                Ingredient.of(Items.CRAFTING_TABLE),
                Ingredient.of(Items.DISPENSER),
                Ingredient.of(Items.BRICKS))
            .build(consumer, loc("soulforge/reagentbuilder"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_CHAINS.get()),
                128.0, 64.0,
                Ingredient.of(Items.IRON_BARS),
                Ingredient.of(Items.ENDER_PEARL),
                Ingredient.of(Items.GLASS_BOTTLE),
                Ingredient.of(Items.END_STONE))
            .build(consumer, loc("soulforge/reagentchains"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_CONSUMPTION.get()),
                128.0, 64.0,
                Ingredient.of(Items.IRON_PICKAXE),
                Ingredient.of(Items.IRON_PICKAXE),
                Ingredient.of(Items.IRON_PICKAXE),
                Ingredient.of(Items.IRON_PICKAXE))
            .build(consumer, loc("soulforge/reagentconsumption"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_EQUIVALENCY.get()),
                128.0, 64.0,
                Ingredient.of(Items.PURPLE_DYE),
                Ingredient.of(Items.DIAMOND),
                Ingredient.of(Items.EMERALD),
                Ingredient.of(Items.QUARTZ_BLOCK))
            .build(consumer, loc("soulforge/reagentequivalency"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_FREE_SOUL.get()),
                256.0, 128.0,
                Ingredient.of(Items.SOUL_SAND),
                Ingredient.of(Items.SOUL_SOIL),
                Ingredient.of(Items.PHANTOM_MEMBRANE),
                Ingredient.of(Items.TOTEM_OF_UNDYING))
            .build(consumer, loc("soulforge/reagentfreesoul"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_HEAVENLY_WRATH.get()),
                256.0, 128.0,
                Ingredient.of(Items.FEATHER),
                Ingredient.of(Items.PHANTOM_MEMBRANE),
                Ingredient.of(Items.SHULKER_SHELL),
                Ingredient.of(Items.ANVIL))
            .build(consumer, loc("soulforge/reagentheavelywrath"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(IronsSpellsCompat.REAGENT_CRIMSON_WILL.get()),
                128.0, 64.0,
                Ingredient.of(Items.NETHER_WART),
                Ingredient.of(Items.CRIMSON_FUNGUS),
                Ingredient.of(itemFromMod("irons_spellbooks", "blood_rune")),
                Ingredient.of(Items.DEEPSLATE))
            .build(consumer, loc("soulforge/reagentcrimsonwill"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_LEACH.get()),
                64.0, 32.0,
                Ingredient.of(Items.NETHER_WART),
                Ingredient.of(Items.CRIMSON_FUNGUS),
                Ingredient.of(ItemTags.SAPLINGS),
                Ingredient.of(Items.VINE))
            .build(consumer, loc("soulforge/reagentleach"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_REMEDIUM.get()),
                128.0, 64.0,
                Ingredient.of(Items.MILK_BUCKET),
                Ingredient.of(Items.GOLDEN_APPLE),
                Ingredient.of(Items.GLISTERING_MELON_SLICE),
                Ingredient.of(Items.HONEY_BOTTLE))
            .build(consumer, loc("soulforge/reagentremendium"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_REPARARE.get()),
                128.0, 64.0,
                Ingredient.of(Items.ANVIL),
                Ingredient.of(Items.DIAMOND),
                Ingredient.of(Items.IRON_INGOT),
                Ingredient.of(Items.GRINDSTONE))
            .build(consumer, loc("soulforge/reagentreparare"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_STORM.get()),
                64.0, 32.0,
                Ingredient.of(Items.SAND),
                Ingredient.of(Items.WATER_BUCKET),
                Ingredient.of(Items.FISHING_ROD),
                Ingredient.of(Items.GHAST_TEAR))
            .build(consumer, loc("soulforge/reagentstorm"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_TEMPORAL_DOMINANCE.get()),
                1024.0, 512.0,
                Ingredient.of(Items.ECHO_SHARD),
                Ingredient.of(Items.CLOCK),
                Ingredient.of(Items.NETHERITE_INGOT),
                Ingredient.of(Items.CHORUS_FRUIT))
            .build(consumer, loc("soulforge/reagenttemporaldominance"));

        TartaricForgeRecipeBuilder.tartaricForge(
                new ItemStack(AnimusItems.REAGENT_TRANSPOSITION.get()),
                128.0, 64.0,
                Ingredient.of(Items.END_STONE),
                Ingredient.of(Items.ENDER_PEARL),
                Ingredient.of(Items.OBSIDIAN),
                Ingredient.of(Items.CHEST))
            .build(consumer, loc("soulforge/reagenttransposition"));
    }

    private void buildArrayRecipes(Consumer<FinishedRecipe> consumer) {
        // Sigils with reinforced slate (tier 2)
        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_BOUNDLESS_NATURE.get()),
                Ingredient.of(itemFromMod("bloodmagic", "reinforcedslate")),
                new ItemStack(AnimusItems.SIGIL_BOUNDLESS_NATURE.get()),
                ARRAY_GROWTH)
            .build(consumer, loc("array/sigil_boundless_nature"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(IronsSpellsCompat.REAGENT_CRIMSON_WILL.get()),
                Ingredient.of(itemFromMod("bloodmagic", "reinforcedslate")),
                new ItemStack(IronsSpellsCompat.SIGIL_CRIMSON_WILL.get()),
                ARRAY_MOBSACRIFICE)
            .build(consumer, loc("array/sigil_crimson_will"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_BUILDER.get()),
                Ingredient.of(itemFromMod("bloodmagic", "reinforcedslate")),
                new ItemStack(AnimusItems.SIGIL_BUILDER.get()),
                ARRAY_LAPUTA)
            .build(consumer, loc("array/sigil_builder"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_EQUIVALENCY.get()),
                Ingredient.of(itemFromMod("bloodmagic", "reinforcedslate")),
                new ItemStack(AnimusItems.SIGIL_EQUIVALENCY.get()),
                ARRAY_FURNACE)
            .build(consumer, loc("array/sigil_equivalency"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_LEACH.get()),
                Ingredient.of(itemFromMod("bloodmagic", "reinforcedslate")),
                new ItemStack(AnimusItems.SIGIL_LEACH.get()),
                ARRAY_MOBSACRIFICE)
            .build(consumer, loc("array/sigil_leach"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_FIST.get()),
                Ingredient.of(itemFromMod("bloodmagic", "reinforcedslate")),
                new ItemStack(AnimusItems.SIGIL_MONK.get()),
                ARRAY_FASTMINER) // Using fast miner texture for the monk sigil
            .build(consumer, loc("array/sigil_monk"));

        // Sigils with imbued slate (tier 3)
        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_CHAINS.get()),
                Ingredient.of(itemFromMod("bloodmagic", "infusedslate")),
                new ItemStack(AnimusItems.SIGIL_CHAINS.get()),
                ARRAY_BINDING)
            .build(consumer, loc("array/sigil_chains"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_CONSUMPTION.get()),
                Ingredient.of(itemFromMod("bloodmagic", "infusedslate")),
                new ItemStack(AnimusItems.SIGIL_CONSUMPTION.get()),
                ARRAY_VOID)
            .build(consumer, loc("array/sigil_consumption"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_REMEDIUM.get()),
                Ingredient.of(itemFromMod("bloodmagic", "infusedslate")),
                new ItemStack(AnimusItems.SIGIL_REMEDIUM.get()),
                ARRAY_LIGHT)
            .build(consumer, loc("array/sigil_remedium"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_REPARARE.get()),
                Ingredient.of(itemFromMod("bloodmagic", "infusedslate")),
                new ItemStack(AnimusItems.SIGIL_REPARARE.get()),
                ARRAY_FASTMINER)
            .build(consumer, loc("array/sigil_reparare"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_STORM.get()),
                Ingredient.of(itemFromMod("bloodmagic", "infusedslate")),
                new ItemStack(AnimusItems.SIGIL_STORM.get()),
                ARRAY_WATER)
            .build(consumer, loc("array/sigil_storm"));

        // Sigils with demonic slate (tier 4)
        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_FREE_SOUL.get()),
                Ingredient.of(itemFromMod("bloodmagic", "demonslate")),
                new ItemStack(AnimusItems.SIGIL_FREE_SOUL.get()),
                ARRAY_TELEPORT)
            .build(consumer, loc("array/sigil_free_soul"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_HEAVENLY_WRATH.get()),
                Ingredient.of(itemFromMod("bloodmagic", "demonslate")),
                new ItemStack(AnimusItems.SIGIL_HEAVENLY_WRATH.get()),
                ARRAY_LIGHTNING)
            .build(consumer, loc("array/sigil_heavenly_wrath"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_TEMPORAL_DOMINANCE.get()),
                Ingredient.of(itemFromMod("bloodmagic", "demonslate")),
                new ItemStack(AnimusItems.SIGIL_TEMPORAL_DOMINANCE.get()),
                ARRAY_MOON)
            .build(consumer, loc("array/sigil_temporal_dominance"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.REAGENT_TRANSPOSITION.get()),
                Ingredient.of(itemFromMod("bloodmagic", "demonslate")),
                new ItemStack(AnimusItems.SIGIL_TRANSPOSITION.get()),
                ARRAY_TELEPORTATION)
            .build(consumer, loc("array/sigil_transposition"));

        // Other array recipes
        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(AnimusItems.SPEAR_DIAMOND.get()),
                Ingredient.of(itemFromMod("bloodmagic", "reagentbinding")),
                new ItemStack(AnimusItems.SPEAR_BOUND.get()),
                ARRAY_SPIKE)
            .build(consumer, loc("array/spear_bound"));

        AlchemyArrayRecipeBuilder.array(
                Ingredient.of(itemFromMod("bloodmagic", "soulscythe")),
                Ingredient.of(itemFromMod("malum", "soul_stained_steel_ingot")),
                new ItemStack(AnimusItems.RUNIC_SENTIENT_SCYTHE.get()),
                ARRAY_AIR)
            .build(consumer, loc("array/runic_sentient_scythe"));
    }

    private ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, path);
    }

    private ItemLike itemFromMod(String modid, String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(modid, name));
    }
}
