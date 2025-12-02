package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusRecipeSerializers;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;

import java.util.function.Consumer;

public class AnimusRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public AnimusRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        // Spear recipes (Roman javelins)
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

        // Sentient Spear - crafted in Soul Forge with Diamond Spear + Petty Tartaric Gem
        // Recipe is in data/animus/recipes/soulforge/spear_sentient.json

        // Sanguine Diviner - crafted in Tier 2 Blood Altar with Ritual Diviner
        // Recipe is in data/animus/recipes/altar/sanguine_diviner.json

        // Blood Apple recipe - simple shapeless recipe
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, AnimusItems.BLOOD_APPLE.get())
            .requires(Items.APPLE)
            .requires(Items.REDSTONE)
            .requires(Items.REDSTONE)
            .unlockedBy("has_apple", has(Items.APPLE))
            .save(consumer);

        // Blood Wood Planks from Log (1 log = 4 planks)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get(), 4)
            .requires(AnimusBlocks.BLOCK_BLOOD_WOOD.get())
            .unlockedBy("has_blood_wood", has(AnimusBlocks.BLOCK_BLOOD_WOOD.get()))
            .save(consumer);

        // Blood Wood Planks from Stripped Log (1 log = 4 planks)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, AnimusItems.BLOCK_BLOOD_WOOD_PLANKS.get(), 4)
            .requires(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get())
            .unlockedBy("has_stripped_blood_wood", has(AnimusBlocks.BLOCK_BLOOD_WOOD_STRIPPED.get()))
            .save(consumer, ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "blood_wood_planks_from_stripped"));

        // Key of Binding unbinding recipe (shapeless special recipe)
        SpecialRecipeBuilder.special(AnimusRecipeSerializers.KEY_UNBINDING.get())
            .save(consumer, ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "key_binding_unbind").toString());
    }

    private net.minecraft.world.level.ItemLike itemFromMod(String modid, String name) {
        // Use BuiltInRegistries to get items from other mods
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath(modid, name)
        );
    }
}
