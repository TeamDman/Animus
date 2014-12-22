package com.TeamDman_9201.nova.Slots;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotLight extends Slot {

  int slotIndex;

  public SlotLight(IInventory inv, int slot, int x, int y) {
    super(inv, slot, x, y);
    this.slotIndex = slot;
    // TODO Auto-generated constructor stub
  }

  /**
   * Helper method to put a stack in the slot.
   */
  private boolean doesItFit(ItemStack item) {
    return Block.getBlockFromItem(item.getItem()).getLightValue() > 0 ? true : false;
  }

  @Override
  public boolean isItemValid(ItemStack check) {
    return doesItFit(check);
  }
//	@Override
//	public void putStack(ItemStack item)
//	{
//		if (doesItFit(item)) {
//			this.inventory.setInventorySlotContents(this.slotIndex, item);
//			this.onSlotChanged();
//		}
//	}
}
