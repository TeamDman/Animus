package com.teamdman.animus.recipes;

import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusRecipeSerializers;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Recipe that unbinds a Key of Binding by removing its binding NBT data
 * Shapeless recipe - just place the Key of Binding in the crafting grid
 */
public class KeyUnbindingRecipe extends CustomRecipe {

    public KeyUnbindingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int keyCount = 0;
        int otherCount = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

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
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        // Find the Key of Binding in the grid
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);

            if (stack.getItem() == AnimusItems.KEY_BINDING.get()) {
                // Create a new stack without NBT data (unbound)
                ItemStack result = new ItemStack(AnimusItems.KEY_BINDING.get());
                // Don't copy NBT - this removes the binding
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
