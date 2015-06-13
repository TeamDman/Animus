package com.teamdman_9201.nova.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Created by TeamDman on 2015-04-11.
 */
public class SlotOutput extends Slot {
    int slotIndex;

    public SlotOutput(IInventory inv, int slot, int x, int y) {
        super(inv, slot, x, y);
        this.slotIndex = slot;
    }

    @Override
    public boolean isItemValid(ItemStack check) {
        return false;
    }
}
