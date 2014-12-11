package com.TeamDman_9201.nova.Slots;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotLightUpgrade extends Slot {
	public SlotLightUpgrade(IInventory inv, int slot, int x, int y) {
		super(inv, slot, x, y);
		// TODO Auto-generated constructor stub
	}
	private boolean doesItFit(ItemStack item) {
		return false;//return (item.isItemEqual(torch) ? true : (item.isItemEqual(glowstone) ? true : false));
	}
	@Override
	public boolean isItemValid(ItemStack check) {
		return doesItFit(check);
	}
}
