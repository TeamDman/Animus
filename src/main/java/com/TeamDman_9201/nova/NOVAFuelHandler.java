package com.TeamDman_9201.nova;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.IFuelHandler;

public class NOVAFuelHandler implements IFuelHandler {
	@Override
	public int getBurnTime(ItemStack fuel) {
		if (fuel.isItemEqual(new ItemStack(NOVA.superCoal)) == true) return 128000;
		return 0;
		//Item superCoal = new SuperCoal();
		//return fuel==new ItemStack(superCoal)?12800:0; //superCoal.getUnlocalizedName() ? 12800 : 0;
	}
}
