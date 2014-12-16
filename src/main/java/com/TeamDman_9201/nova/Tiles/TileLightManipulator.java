package com.TeamDman_9201.nova.Tiles;

import java.util.ArrayList;

import com.TeamDman_9201.nova.Threads.ThreadLightManipulator;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

public class TileLightManipulator extends TileEntity implements ISidedInventory {
	private ThreadLightManipulator	thread						= new ThreadLightManipulator(this);
	private int						iteration					= 0;
	private int						speed						= 25;
	private boolean					isActive					= false;
	/**
	 * The ItemStacks that hold the items currently being used in the light
	 * manipulator
	 */
	private ItemStack[]				LightManipulatorItemStacks	= new ItemStack[8];
	private ArrayList<int[]>		chunkContents;

	private String					containerName				= "Light Manipulator";

	/**
	 * Returns the number of slots in the inventory.
	 */
	public int getSizeInventory() {
		return this.LightManipulatorItemStacks.length;
	}

	/**
	 * Returns the stack in slot i
	 */
	public ItemStack getStackInSlot(int par1) {
		return this.LightManipulatorItemStacks[par1];
	}

	/**
	 * Removes from an inventory slot (first arg) up to a specified number
	 * (second arg) of items and returns them in a new stack.
	 */
	public ItemStack decrStackSize(int slot, int ammount) {
		if (this.LightManipulatorItemStacks[slot] != null) {
			ItemStack itemstack;

			if (this.LightManipulatorItemStacks[slot].stackSize <= ammount) {
				itemstack = this.LightManipulatorItemStacks[slot];
				this.LightManipulatorItemStacks[slot] = null;
				return itemstack;
			} else {
				itemstack = this.LightManipulatorItemStacks[slot].splitStack(ammount);

				if (this.LightManipulatorItemStacks[slot].stackSize == 0) {
					this.LightManipulatorItemStacks[slot] = null;
				}

				return itemstack;
			}
		} else {
			return null;
		}
	}

	/**
	 * When some containers are closed they call this on each slot, then drop
	 * whatever it returns as an EntityItem - like when you close a workbench
	 * GUI.
	 */
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (this.LightManipulatorItemStacks[slot] != null) {
			ItemStack itemstack = this.LightManipulatorItemStacks[slot];
			this.LightManipulatorItemStacks[slot] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	/**
	 * Sets the given item stack to the specified slot in the inventory (can be
	 * crafting or armor sections).
	 */
	public void setInventorySlotContents(int slot, ItemStack par2ItemStack) {
		this.LightManipulatorItemStacks[slot] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit()) {
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}
	}

	/**
	 * Returns the name of the inventory
	 */
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.containerName : "container.furnace";
	}

	/**
	 * Returns if the inventory is named
	 */
	public boolean hasCustomInventoryName() {
		return this.containerName != null && this.containerName.length() > 0;
	}

	public void setName(String name) {
		this.containerName = name;
	}

	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		NBTTagList nbttaglist = tagCompound.getTagList("Items", 10);
		this.LightManipulatorItemStacks = new ItemStack[this.getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			byte slot = nbttagcompound1.getByte("Slot");

			if (slot >= 0 && slot < this.LightManipulatorItemStacks.length) {
				this.LightManipulatorItemStacks[slot] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}

		if (tagCompound.hasKey("CustomName", 8)) {
			this.containerName = tagCompound.getString("CustomName");
		}
	}

	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		NBTTagList NBTList = new NBTTagList();

		for (int slot = 0; slot < this.LightManipulatorItemStacks.length; ++slot) {
			if (this.LightManipulatorItemStacks[slot] != null) {
				NBTTagCompound NBTCompound = new NBTTagCompound();
				NBTCompound.setByte("Slot", (byte) slot);
				this.LightManipulatorItemStacks[slot].writeToNBT(NBTCompound);
				NBTList.appendTag(NBTCompound);
			}
		}

		tagCompound.setTag("Items", NBTList);

		if (this.hasCustomInventoryName()) {
			tagCompound.setString("CustomName", this.containerName);
		}
	}
	
	public void commence() {
		if (!this.worldObj.isRemote && !this.isActive) {
			this.isActive = true;
			chunkContents = thread.getBlocksInChunk(this.worldObj, false, 1);
			System.out.println("[FIRST DEBUG] Should have executed");
		}
	}

	/**
	 * Called every tick. (I Think?)
	 */
	public void updateEntity() {
		if (!this.worldObj.isRemote) {
			if (chunkContents != null && chunkContents.size() > 0) {
				// System.out.println("We got lighting to fix!");
				for (int i = 0; i < speed; i++) {
					if (chunkContents.size()>iteration) {
						int[] coords = chunkContents.get(iteration);
						int x = coords[0];
						int y = coords[1];
						int z = coords[2];
						boolean placed = thread.correctLight(this.worldObj, x, y, z);
						iteration++;
						if (iteration % 100 == 0) {
							System.out.printf("Just scanned %d blocks, recently at (%d,%d,%d).\n", iteration, x, y, z);
						}
						if (placed) {
							break;
						}
					} else {
						chunkContents = null;
						iteration = 0;
						System.out.println("We is done fixing the lights.");
						this.isActive = false;
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns the maximum stack size for a inventory slot.
	 */
	public int getInventoryStackLimit() {
		return 64;
	}

	/**
	 * Do not make give this method the name canInteractWith because it clashes
	 * with Container
	 */
	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer) {
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq(
				(double) this.xCoord + 0.5D, (double) this.yCoord + 0.5D, (double) this.zCoord + 0.5D) <= 64.0D;
	}

	public void openInventory() {
	}

	public void closeInventory() {
	}

	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring
	 * stack size) into the given slot.
	 */
	public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
		return true;
	}

	/**
	 * Returns an array containing the indices of the slots that can be accessed
	 * by automation on the given side of this block.
	 */
	public int[] getAccessibleSlotsFromSide(int slot) {
		// return par1 == 0 ? slotsBottom : (par1 == 1 ? slotsTop : slotsSides);
		return new int[] { 0, 4 };
	}

	/**
	 * Returns true if automation can insert the given item in the given slot
	 * from the given side. Args: Slot, item, side
	 */
	public boolean canInsertItem(int par1, ItemStack par2ItemStack, int par3) {
		return this.isItemValidForSlot(par1, par2ItemStack);
	}

	/**
	 * Returns true if automation can extract the given item in the given slot
	 * from the given side. Args: Slot, item, side
	 */
	public boolean canExtractItem(int par1, ItemStack par2ItemStack, int par3) {
		return true;
		// return par3 != 0 || par1 != 1 || par2ItemStack.getItem() ==
		// Items.bucket;
	}
}
