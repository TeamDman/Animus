package com.teamdman_9201.nova.slots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/**
 * Created by TeamDman on 2015-08-01.
 */
public class SlotYup extends Slot {
    public SlotYup(IInventory inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return true; //YUP :D
    }
}
