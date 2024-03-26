package com.teamdman_9201.nova.handlers;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.IFuelHandler;

public class NOVAFuelHandler implements IFuelHandler {

    @Override
    public int getBurnTime(ItemStack fuel) {
        if (fuel.isItemEqual(new ItemStack(NOVA.itemUnstableCoal)) == true) {
            return 1280000;
        }
        return 0;
        //Item itemSuperCoal = new SuperCoal();
        //return fuel==new ItemStack(itemSuperCoal)?12800:0; //itemSuperCoal.getUnlocalizedName() ? 12800 : 0;
    }

}