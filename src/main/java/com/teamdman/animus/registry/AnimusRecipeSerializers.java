package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.recipes.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;


public class AnimusRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Constants.Mod.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<KeyUnbindingRecipe>> KEY_UNBINDING =
        RECIPE_SERIALIZERS.register("key_unbinding",
            () -> new SimpleCraftingRecipeSerializer<>(KeyUnbindingRecipe::new));

    // Imperfect ritual serializers
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RegressionRitualRecipe>> REGRESSION_RITUAL =
        RECIPE_SERIALIZERS.register("regression_ritual",
            RegressionRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HungerRitualRecipe>> HUNGER_RITUAL =
        RECIPE_SERIALIZERS.register("hunger_ritual",
            HungerRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EnhancementRitualRecipe>> ENHANCEMENT_RITUAL =
        RECIPE_SERIALIZERS.register("enhancement_ritual",
            EnhancementRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ReductionRitualRecipe>> REDUCTION_RITUAL =
        RECIPE_SERIALIZERS.register("reduction_ritual",
            ReductionRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BoundlessSkiesRitualRecipe>> BOUNDLESS_SKIES_RITUAL =
        RECIPE_SERIALIZERS.register("boundless_skies_ritual",
            BoundlessSkiesRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ClearSkiesRitualRecipe>> CLEAR_SKIES_RITUAL =
        RECIPE_SERIALIZERS.register("clear_skies_ritual",
            ClearSkiesRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NeptuneBlessingRitualRecipe>> NEPTUNE_BLESSING_RITUAL =
        RECIPE_SERIALIZERS.register("neptune_blessing_ritual",
            NeptuneBlessingRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<WardenRitualRecipe>> WARDEN_RITUAL =
        RECIPE_SERIALIZERS.register("warden_ritual",
            WardenRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ManasteelSoulRitualRecipe>> MANASTEEL_SOUL_RITUAL =
        RECIPE_SERIALIZERS.register("manasteel_soul_ritual",
            ManasteelSoulRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SoulStainedBloodRitualRecipe>> SOUL_STAINED_BLOOD_RITUAL =
        RECIPE_SERIALIZERS.register("soul_stained_blood_ritual",
            SoulStainedBloodRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MagiRitualRecipe>> MAGI_RITUAL =
        RECIPE_SERIALIZERS.register("magi_ritual",
            MagiRitualRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<IronHeartRitualRecipe>> IRON_HEART_RITUAL =
        RECIPE_SERIALIZERS.register("iron_heart_ritual",
            IronHeartRitualRecipe.Serializer::new);
}
