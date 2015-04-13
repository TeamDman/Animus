package com.TeamDman_9201.nova.Containers;

import com.TeamDman_9201.nova.GenericInventory;
import com.TeamDman_9201.nova.Slots.SlotOutput;
import com.TeamDman_9201.nova.Slots.SlotUpgrade;
import com.TeamDman_9201.nova.Tiles.TileCobblizer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Created by TeamDman on 2015-04-04.
 */
public class ContainerCobblizer extends Container {

  private GenericInventory tile;

  public ContainerCobblizer(InventoryPlayer playerInventory, TileCobblizer tile) {
    this.tile = tile;
    this.addSlotToContainer(new SlotUpgrade(this.tile, 0, 8, 8));
    this.addSlotToContainer(new Slot(this.tile, 1, 35, 35));
    playerInventory.openInventory();
    for (int y = 0; y < 3; ++y) {
      for (int x = 0; x < 3; ++x) {
        this.addSlotToContainer(
            new SlotOutput(this.tile, y + x * 3 + 2, x * 18 + 100, y * 18 + 17));
      }
    }

    for (int column = 0; column < 3; ++column) {
      for (int row = 0; row < 9; ++row) {
        this.addSlotToContainer(
            new Slot(playerInventory, row + column * 9 + 9, 8 + row * 18, column * 18 + 84));
      }
    }

    for (int column = 0; column < 9; ++column) {
      this.addSlotToContainer(new Slot(playerInventory, column, 8 + column * 18, 142));
    }
  }

  /**
   * Called when a player shift-clicks on a slot. You must override this or you will crash when
   * someone does that.
   */
  public ItemStack transferStackInSlot(EntityPlayer player, int parSlot) {
    ItemStack itemstack = null;
    Slot slot = (Slot) this.inventorySlots.get(parSlot);

    if (slot != null && slot.getHasStack()) {
      ItemStack itemstack1 = slot.getStack();
      itemstack = itemstack1.copy();

      if (parSlot > 11) {
        if (SlotUpgrade.isItemUpgrade(itemstack1)) {
          if (!this.mergeItemStack(itemstack1,0,1, false)) {
            return null;
          }
        } else if  (!this.mergeItemStack(itemstack1, 1, 2, false)) {
          return null;
        }
      } else if (!this.mergeItemStack(itemstack1, 11, 47, false)) {
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
