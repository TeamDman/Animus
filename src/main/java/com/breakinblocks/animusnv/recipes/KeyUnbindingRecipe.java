package com.breakinblocks.animusnv.recipes;

import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.registry.AnimusRecipeSerializers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Recipe that unbinds a Key of Binding by removing its binding data
 * Shapeless recipe - just place the Key of Binding in the crafting grid
 */
public class KeyUnbindingRecipe extends CustomRecipe {

    private final CraftingBookCategory category;

    public KeyUnbindingRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int keyCount = 0;
        int otherCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (!stack.isEmpty()) {
                if (stack.getItem() == AnimusItems.KEY_BINDING.get()) {
                    keyCount++;
                } else {
                    otherCount++;
                }
            }
        }

        return keyCount == 1 && otherCount == 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.getItem() == AnimusItems.KEY_BINDING.get()) {
                ItemStack result = new ItemStack(AnimusItems.KEY_BINDING.get());
                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<KeyUnbindingRecipe> getSerializer() {
        return AnimusRecipeSerializers.KEY_UNBINDING.get();
    }
}
