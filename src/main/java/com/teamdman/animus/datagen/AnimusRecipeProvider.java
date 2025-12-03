package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
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
import wayoftime.bloodmagic.common.data.recipe.builder.BloodAltarRecipeBuilder;
import wayoftime.bloodmagic.common.data.recipe.builder.TartaricForgeRecipeBuilder;

import java.util.function.Consumer;

public class AnimusRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public AnimusRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        buildCraftingRecipes(consumer);
        buildAltarRecipes(consumer);
        buildSoulForgeRecipes(consumer);
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

    private ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, path);
    }

    private ItemLike itemFromMod(String modid, String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(modid, name));
    }
}
