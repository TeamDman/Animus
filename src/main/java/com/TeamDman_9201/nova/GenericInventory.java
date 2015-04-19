package com.TeamDman_9201.nova;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by TeamDman on 2015-04-04.
 */
public class GenericInventory extends TileEntity implements ISidedInventory {

  public ItemStack[] items;
  public int[] slotsTop;
  public int[] slotsBottom;
  public int[] slotsSides;
  public String containerName;

  public GenericInventory(int items, String name, int[] top, int[] bottom, int[] sides) {
    this.items = new ItemStack[items];
    this.containerName = name;
    slotsTop = top;
    slotsBottom = bottom;
    slotsSides = sides;
  }

  public ItemStack insertStack(ItemStack stack,int slot) {
    if (items[slot] == null) {
      items[slot] = stack;
    } else {
      if (items[slot].isItemEqual(stack)) {
        stack.stackSize+=items[slot].stackSize;
        if (stack.stackSize > getInventoryStackLimit()) {
          items[slot] = stack.splitStack(getInventoryStackLimit());
          return stack;
        } else {
          items[slot] = stack;
        }
      }
    }
    return null;
  }

  public void showSlots() {
    for (int i = 0; i < getSizeInventory(); ++i) {
      setInventorySlotContents(i, new ItemStack(Block.getBlockById(i + 1)));
    }
  }

  public ItemStack getStackInSlot(int par1) {
    return this.items[par1];
  }

  public ItemStack decrStackSize(int slot, int amount) {
    if (this.items[slot] != null) {
      ItemStack stack;

      if (this.items[slot].stackSize <= amount) {
        stack = this.items[slot];
        this.items[slot] = null;
        return stack;
      } else {
        stack = this.items[slot].splitStack(amount);

        if (this.items[slot].stackSize == 0) {
          this.items[slot] = null;
        }

        return stack;
      }
    } else {
      return null;
    }
  }

  public void setInventorySlotContents(int slot, ItemStack stack) {
    this.items[slot] = stack;

    if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
      stack.stackSize = this.getInventoryStackLimit();
    }
  }

  public String getInventoryName() {
    return this.hasCustomInventoryName() ? this.containerName : "container.unknown";
  }

  public boolean hasCustomInventoryName() {
    return this.containerName != null && this.containerName.length() > 0;
  }

  public void setName(String name) {
    this.containerName = name;
  }

  public int getSizeInventory() {
    return this.items.length;
  }

  public int getInventoryStackLimit() {
    return 64;
  }

  public boolean isItemValidForSlot(int slot, ItemStack item) {
    return true;
  }

  public int[] getAccessibleSlotsFromSide(int side) {
    return side == 0 ? slotsBottom : (side == 1 ? slotsTop : slotsSides);
//    return new int[]{0, this.getSizeInventory()-1};
  }

  public boolean canInsertItem(int slot, ItemStack item, int side) {
    return this.isItemValidForSlot(slot, item);
  }

  public boolean canExtractItem(int slot, ItemStack item, int side) {
    return true;
  }

  public boolean isUseableByPlayer(EntityPlayer player) {
    return (this.getWorldObj().getTileEntity(xCoord, yCoord, zCoord) == this) && (
        player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64.0D);
  }

  public ItemStack getStackInSlotOnClosing(int slot) {
    if (this.items[slot] != null) {
      ItemStack itemstack = this.items[slot];
      this.items[slot] = null;
      return itemstack;
    } else {
      return null;
    }
  }

  public void openInventory() {
  }

  public void closeInventory() {
  }

  public void readFromNBT(NBTTagCompound tagCompound) {
    super.readFromNBT(tagCompound);
    NBTTagList nbttaglist = tagCompound.getTagList("Items", 10);
    this.items = new ItemStack[this.getSizeInventory()];

    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
      NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
      byte slot = nbttagcompound1.getByte("Slot");

      if (slot >= 0 && slot < this.items.length) {
        this.items[slot] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
      }
    }

    if (tagCompound.hasKey("CustomName", 8)) {
      this.containerName = tagCompound.getString("CustomName");
    }
  }

  public void writeToNBT(NBTTagCompound tagCompound) {
    super.writeToNBT(tagCompound);
    NBTTagList NBTList = new NBTTagList();

    for (int slot = 0; slot < this.items.length; ++slot) {
      if (this.items[slot] != null) {
        NBTTagCompound NBTCompound = new NBTTagCompound();
        NBTCompound.setByte("Slot", (byte) slot);
        this.items[slot].writeToNBT(NBTCompound);
        NBTList.appendTag(NBTCompound);
      }
    }

    tagCompound.setTag("Items", NBTList);

    if (this.hasCustomInventoryName()) {
      tagCompound.setString("CustomName", this.containerName);
    }
  }
}
