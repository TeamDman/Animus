package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.recipes.ImperfectRitualRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AnimusRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(
        Registries.RECIPE_TYPE,
        Constants.Mod.MODID
    );

    public static final RegistryObject<RecipeType<ImperfectRitualRecipe>> IMPERFECT_RITUAL_TYPE = RECIPE_TYPES.register(
        "imperfect_ritual",
        () -> new RecipeType<ImperfectRitualRecipe>() {
            @Override
            public String toString() {
                return Constants.Mod.MODID + ":imperfect_ritual";
            }
        }
    );
}
