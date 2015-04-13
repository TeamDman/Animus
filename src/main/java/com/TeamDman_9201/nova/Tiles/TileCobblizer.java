package com.TeamDman_9201.nova.Tiles;

import com.TeamDman_9201.nova.GenericInventory;

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
  public int buffer = 0;

  public TileCobblizer() {
    super(110, "Cobblizer");
  }

  @Override
  public void updateEntity() {
    super.updateEntity();
    if (!worldObj.isRemote) {
      if (items[1] != null && buffer == 0) {
        System.out.println("GETTING COMPONENTS FOR: " + items[1].getDisplayName());
        buffer += getComponents(items[1], 0, new HashMap());
        decrStackSize(1, 1);
      }
      if (buffer > 0) {
        for (int slot = 2; slot < 13; ++slot) {
          int size = buffer > getInventoryStackLimit() ? getInventoryStackLimit() : buffer;
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
//https://github.com/sinkillerj/ProjectE/blob/master/src/main/java/moze_intel/projecte/emc/mappers/CraftingMapper.java
/*        Iterable recipeItems = null;
        if (recipe instanceof ShapedRecipes) {
          recipeItems = Arrays.asList(((ShapedRecipes) recipe).recipeItems);
        } else if (recipe instanceof ShapelessRecipes) {
          recipeItems = ((ShapelessRecipes) recipe).recipeItems;
        }
        if (recipeItems == null) {
          continue;
        }
        for (Object component : recipeItems) {
          if (component == null) {
            continue;
          }
          if (component instanceof ItemStack) {
            ItemStack recipeItem = (ItemStack) component;
            rtn += getComponents(recipeItem, layer);
          }
        }
*/

  public int getComponents(ItemStack input, int layer, HashMap indexed) {
    System.out.println("RECURSIVE Level: " + layer + " | " + input.getDisplayName());
    if (indexed.get(input) != null) {
      System.out.println("Index found for " + input.getDisplayName());
      return (Integer) indexed.get(input);
    }
    if (layer > 8) {
      return 0;
    }
    layer++;
    int rtn = 1;
    List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
    for (IRecipe recipe : recipes) {
      if (recipe.getRecipeOutput() != null && recipe.getRecipeOutput().isItemEqual(input)) {
        System.out.println("FOUND MATCH: " + input.getDisplayName());
        rtn += recipe.getRecipeSize();
        List components = null;
        if (recipe instanceof ShapelessRecipes) {
          components = ((ShapelessRecipes) recipe).recipeItems;
        } else if (recipe instanceof ShapedRecipes) {
          components = Arrays.asList(((ShapedRecipes) recipe).recipeItems);
        } else if (recipe instanceof ShapedOreRecipe) {
          components = Arrays.asList(((ShapedOreRecipe) recipe).getInput());
        } else {
          System.out.println("Not a recipe, " + recipe.getClass().getName());
          continue;
        }
        if (components == null) {
          System.out.println("No components");
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
      } // End of matching recursion
    }  // End of recipe iterations

    return rtn;
  }
}