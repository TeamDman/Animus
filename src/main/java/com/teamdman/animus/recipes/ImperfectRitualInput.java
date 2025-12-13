package com.teamdman.animus.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Empty RecipeInput for imperfect rituals
 * The actual matching is done via block state, not item input
 */
public class ImperfectRitualInput implements RecipeInput {
    public static final ImperfectRitualInput EMPTY = new ImperfectRitualInput();

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }
}
