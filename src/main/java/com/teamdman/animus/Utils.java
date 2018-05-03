package com.teamdman.animus;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.ItemHandlerHelper;

import static WayofTime.bloodmagic.util.Utils.combineStacks;

public class Utils {

	public static boolean canInsertStackFullyIntoInventory(ItemStack stack, IInventory inventory, EnumFacing dir) {
		return canInsertStackFullyIntoInventory(stack, inventory, dir, false, 0);
	}

	public static boolean canInsertStackFullyIntoInventory(ItemStack stack, IInventory inventory, EnumFacing dir, boolean fillToLimit, int limit) {
		if (stack.isEmpty()) {
			return true;
		}

		int itemsLeft = stack.getCount();

		boolean[] canBeInserted = new boolean[inventory.getSizeInventory()];

		if (inventory instanceof ISidedInventory) {
			int[] array = ((ISidedInventory) inventory).getSlotsForFace(dir);
			for (int in : array) {
				canBeInserted[in] = inventory.isItemValidForSlot(in, stack) && ((ISidedInventory) inventory).canInsertItem(in, stack, dir);
			}
		} else {
			for (int i = 0; i < canBeInserted.length; i++) {
				canBeInserted[i] = inventory.isItemValidForSlot(i, stack);
			}
		}

		int numberMatching = 0;

		if (fillToLimit) {
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				if (!canBeInserted[i]) {
					continue;
				}

				ItemStack invStack = inventory.getStackInSlot(i);

				if (!invStack.isEmpty() && ItemHandlerHelper.canItemStacksStack(stack, invStack)) {
					numberMatching += invStack.getCount();
				}
			}
		}

		if (fillToLimit && limit < stack.getCount() + numberMatching) {
			return false;
		}

		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			if (!canBeInserted[i]) {
				continue;
			}

			ItemStack invStack = inventory.getStackInSlot(i);
			boolean canCombine = ItemHandlerHelper.canItemStacksStack(stack, invStack) || invStack == ItemStack.EMPTY;
			if (canCombine) {
				if (invStack.isEmpty()) {
					itemsLeft = 0;
				} else {
					itemsLeft -= (invStack.getMaxStackSize() - invStack.getCount());
				}
			}

			if (itemsLeft <= 0) {
				return true;
			}
		}

		return false;
	}


	public static ItemStack insertStackIntoInventory(ItemStack stack, IInventory inventory, EnumFacing dir) {
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		boolean[] canBeInserted = new boolean[inventory.getSizeInventory()];

		if (inventory instanceof ISidedInventory) {
			int[] array = ((ISidedInventory) inventory).getSlotsForFace(dir);
			for (int in : array) {
				canBeInserted[in] = inventory.isItemValidForSlot(in, stack) && ((ISidedInventory) inventory).canInsertItem(in, stack, dir);
			}
		} else {
			for (int i = 0; i < canBeInserted.length; i++) {
				canBeInserted[i] = inventory.isItemValidForSlot(i, stack);
			}
		}

		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			if (!canBeInserted[i]) {
				continue;
			}
			if (inventory.getStackInSlot(i) == ItemStack.EMPTY) {
				inventory.setInventorySlotContents(i, stack.splitStack(64));
			} else {
				ItemStack[] combinedStacks = combineStacks(stack, inventory.getStackInSlot(i));
				stack = combinedStacks[0];
				inventory.setInventorySlotContents(i, combinedStacks[1]);
			}
			if (stack.isEmpty()) {
				return ItemStack.EMPTY;
			}
		}

		return stack;
	}
}
