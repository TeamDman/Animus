package com.teamdman.animus.recipes;

import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.CraftingContainer;
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

    public KeyUnbindingRecipe(CraftingBookCategory category) {
        super(category);
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

        // Match if there's exactly one Key of Binding and nothing else
        return keyCount == 1 && otherCount == 0;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        // Find the Key of Binding in the grid
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.getItem() == AnimusItems.KEY_BINDING.get()) {
                // Create a new stack without data components (unbound)
                ItemStack result = new ItemStack(AnimusItems.KEY_BINDING.get());
                // Don't copy components - this removes the binding
                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        // Can be crafted in any crafting grid (even 2x2)
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AnimusRecipeSerializers.KEY_UNBINDING.get();
    }
}
