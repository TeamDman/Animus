package com.TeamDman.nova.Containers;

import com.TeamDman.nova.Slots.SlotUpgrade;
import com.TeamDman.nova.GenericInventory;
import com.TeamDman.nova.Tiles.TileDirtChest;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Created by TeamDman on 2015-04-04.
 */
public class ContainerDirtChest extends Container {

  private GenericInventory tile;

  public ContainerDirtChest(InventoryPlayer playerInventory, TileDirtChest tile) {
    this.tile = tile;
    this.addSlotToContainer(new Slot(this.tile, 0, 80, 19));

    for (int column = 0; column < 3; ++column) {
      for (int row = 0; row < 9; ++row) {
        this.addSlotToContainer(
            new Slot(playerInventory, row + column * 9 + 9, 8 + row * 18, column * 18 + 48));
      }
    }

    for (int column = 0; column < 9; ++column) {
      this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 106));
    }
  }

  public ItemStack transferStackInSlot(EntityPlayer player, int parSlot) {
    ItemStack itemstack = null;
    Slot slot = (Slot) this.inventorySlots.get(parSlot);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (parSlot > tile.getSizeInventory()-1) {
        if (SlotUpgrade.isItemUpgrade(itemstack1)) {
          if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
            return null;
          }
        } else if (!this.mergeItemStack(itemstack1, 1, 2, false)) {
          return null;
        }
      } else if (!this.mergeItemStack(itemstack1, tile.getSizeInventory()-1, inventorySlots.size(), false)) {
        return null;
      }

      if (itemstack1.stackSize == 0) {
        slot.putStack((ItemStack) null);
      } else {
        slot.onSlotChanged();
      }
    }

    return itemstack;
  }

//  public void addCraftingToCrafters(ICrafting par1ICrafting) {
//    super.addCraftingToCrafters(par1ICrafting);
//  }

  @Override
  public boolean canInteractWith(EntityPlayer var1) {
    return true;
  }
}
