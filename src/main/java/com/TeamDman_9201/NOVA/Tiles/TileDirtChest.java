package com.TeamDman.nova.Tiles;

import com.TeamDman.nova.GenericInventory;

import net.minecraft.item.ItemStack;

/**
 * Created by TeamDman on 2015-04-04.
 */
public class TileDirtChest extends GenericInventory {

  public TileDirtChest() {
    super(1, "Dirt Chest 9002", new int[] {0}, new int[] {0},new int[] {0});
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack item) {
    return true; //slot>1?false:slot==1?true:false;
  }
}