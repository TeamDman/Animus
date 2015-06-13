package com.teamdman_9201.nova.slots;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotUpgrade extends Slot {

    public SlotUpgrade(IInventory inv, int slot, int x, int y) {
        super(inv, slot, x, y);
    }

    public static boolean isItemUpgrade(ItemStack check) {
        return check.isItemEqual(new ItemStack(NOVA.itemSlotIdentifier));
    }

    @Override
    public boolean isItemValid(ItemStack check) {
        return check.isItemEqual(new ItemStack(NOVA.itemSlotIdentifier));
    }
}
