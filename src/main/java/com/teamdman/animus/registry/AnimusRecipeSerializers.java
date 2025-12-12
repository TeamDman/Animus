package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.recipes.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Constants.Mod.MODID);

    public static final RegistryObject<RecipeSerializer<KeyUnbindingRecipe>> KEY_UNBINDING =
        RECIPE_SERIALIZERS.register("key_unbinding",
            () -> new SimpleCraftingRecipeSerializer<>(KeyUnbindingRecipe::new));

    // Imperfect ritual serializers
    public static final RegistryObject<RecipeSerializer<RegressionRitualRecipe>> REGRESSION_RITUAL =
        RECIPE_SERIALIZERS.register("regression_ritual",
            RegressionRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<HungerRitualRecipe>> HUNGER_RITUAL =
        RECIPE_SERIALIZERS.register("hunger_ritual",
            HungerRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<EnhancementRitualRecipe>> ENHANCEMENT_RITUAL =
        RECIPE_SERIALIZERS.register("enhancement_ritual",
            EnhancementRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ReductionRitualRecipe>> REDUCTION_RITUAL =
        RECIPE_SERIALIZERS.register("reduction_ritual",
            ReductionRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<BoundlessSkiesRitualRecipe>> BOUNDLESS_SKIES_RITUAL =
        RECIPE_SERIALIZERS.register("boundless_skies_ritual",
            BoundlessSkiesRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ClearSkiesRitualRecipe>> CLEAR_SKIES_RITUAL =
        RECIPE_SERIALIZERS.register("clear_skies_ritual",
            ClearSkiesRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<NeptuneBlessingRitualRecipe>> NEPTUNE_BLESSING_RITUAL =
        RECIPE_SERIALIZERS.register("neptune_blessing_ritual",
            NeptuneBlessingRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<WardenRitualRecipe>> WARDEN_RITUAL =
        RECIPE_SERIALIZERS.register("warden_ritual",
            WardenRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<ManasteelSoulRitualRecipe>> MANASTEEL_SOUL_RITUAL =
        RECIPE_SERIALIZERS.register("manasteel_soul_ritual",
            ManasteelSoulRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<SoulStainedBloodRitualRecipe>> SOUL_STAINED_BLOOD_RITUAL =
        RECIPE_SERIALIZERS.register("soul_stained_blood_ritual",
            SoulStainedBloodRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<MagiRitualRecipe>> MAGI_RITUAL =
        RECIPE_SERIALIZERS.register("magi_ritual",
            MagiRitualRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<IronHeartRitualRecipe>> IRON_HEART_RITUAL =
        RECIPE_SERIALIZERS.register("iron_heart_ritual",
            IronHeartRitualRecipe.Serializer::new);
}
