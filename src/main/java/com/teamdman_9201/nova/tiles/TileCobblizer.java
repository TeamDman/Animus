package com.teamdman_9201.nova.tiles;

import com.teamdman_9201.nova.NOVA;
import com.teamdman_9201.nova.generics.GenericInventory;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by TeamDman on 2015-04-04.
 */
public class TileCobblizer extends GenericInventory {

    //Slot 0 : Upgrades
    //Slot 1 : Input
    //Slot 2-11 : Output
    public int progress = 50;
    public int buffer   = 0;

    public TileCobblizer() {
        super(11, "Cobblizer", new int[]{1}, new int[]{2, 10}, new int[]{2, 10});
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (!worldObj.isRemote) {
            if (items[0] != null && items[0].isItemEqual(new ItemStack(NOVA.itemSlotIdentifier))) {
                showSlots();
                return;
            }
            if (items[1] != null && buffer == 0) {
                buffer += getComponents(items[1], 0, new HashMap());
                decrStackSize(1, 1);
            }
            if (buffer > 0) {
                for (int slot = 2; slot < 11; ++slot) {
                    int size = buffer > getInventoryStackLimit() ? getInventoryStackLimit() :
                            buffer;
                    if (size == 0) {
                        break;
                    }
                    ItemStack remaining;
                    remaining = insertStack(new ItemStack(Blocks.cobblestone, size), slot);
                    if (remaining != null) {
                        buffer += remaining.stackSize;
                    }
                    buffer -= size;
                }
            }
        }
    }

    public int getComponents(ItemStack input, int layer, HashMap indexed) {
        if (indexed.get(input) != null) {
            return (Integer) indexed.get(input);
        }
        if (layer > 8) {
            return 0;
        }
        layer++;
        int           rtn     = 1;
        List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
        for (IRecipe recipe : recipes) {
            if (recipe.getRecipeOutput() != null && recipe.getRecipeOutput().isItemEqual(input)) {
                rtn += recipe.getRecipeSize();
                List components = null;
                if (recipe instanceof ShapelessRecipes) {
                    components = ((ShapelessRecipes) recipe).recipeItems;
                } else if (recipe instanceof ShapedRecipes) {
                    components = Arrays.asList(((ShapedRecipes) recipe).recipeItems);
                } else if (recipe instanceof ShapedOreRecipe) {
                    components = Arrays.asList(((ShapedOreRecipe) recipe).getInput());
                } else {
                    continue;
                }
                if (components == null) {
                    continue;
                }
                Iterator iter = components.iterator();
                while (iter.hasNext()) {
                    Object recipeItem = (iter.next());
                    if (recipeItem instanceof ItemStack) {
                        int inc = getComponents((ItemStack) recipeItem, layer, indexed);
                        indexed.put(recipeItem, new Integer(inc));
                        rtn += inc;
                    } else if (recipeItem instanceof Collection) {
                        Collection recipeItemCollection = ((Collection) recipeItem);
                        if (recipeItemCollection.size() == 1) {
                            Object element = recipeItemCollection.iterator().next();
                            if (element instanceof ItemStack) {
                                rtn += getComponents((ItemStack) element, layer, indexed);
                            }
                            continue;
                        }
                        for (Object option : recipeItemCollection) {
                            if (option instanceof ItemStack) {
                                int inc = getComponents((ItemStack) option, layer, indexed);
                                indexed.put((ItemStack) option, new Integer(inc));
                                rtn += inc;
                            }
                        }
                    }
                }
            }
        }
        return rtn;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack item) {
        return slot > 1 ? false : slot == 1 ? true : false;
    }
}