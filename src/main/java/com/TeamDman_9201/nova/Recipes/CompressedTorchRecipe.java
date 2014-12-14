package com.TeamDman_9201.nova.Recipes;

import java.util.ArrayList;
import java.util.List;

import com.TeamDman_9201.nova.NOVA;

import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class CompressedTorchRecipe implements IRecipe {
	public int[] getData(InventoryCrafting crafting) {
		int rtn[] = { 0, 0, 0, 0 };
		for (int i = 0; i < crafting.getSizeInventory(); i++) {
			ItemStack contents = crafting.getStackInSlot(i);
			// if (contents != null) {
			// System.out.println(contents.getDisplayName() + " " + i);
			// }
			if (contents != null) {
				if (contents.getItem() == new ItemStack(Blocks.torch).getItem()) {
					rtn[0]++;
					rtn[1]++;
				} else if (contents.getItem() == new ItemStack(
						NOVA.compressedTorch).getItem()) {
					rtn[0]++;
					rtn[2]++;
					if (contents.getTagCompound() != null) {
						rtn[3] += contents.getTagCompound().getInteger(
								"Torches");
					}
				}
			}
		}

		return rtn;
	}

	@Override
	public boolean matches(InventoryCrafting crafting, World world) {
		// TODO Auto-generated method stub
		return getData(crafting)[0] > 0;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) {
		// TODO Auto-generated method stub
		// 0=total
		// 1=torches
		// 2=cTorches
		// 3=storedTorches
		ArrayList<String> toolTip = new ArrayList<String>();
		toolTip.add("Test");
		ItemStack returnStack = new ItemStack(NOVA.compressedTorch);
		int parts[] = getData(crafting);
		int storedTorches = parts[1] + parts[3];

		if (parts[0] == 1 && parts[2] == 1) {
			returnStack.stackSize = 2;
			storedTorches /= 2;
		}

		NBTTagCompound nbtData = new NBTTagCompound();
		nbtData.setInteger("Torches", storedTorches);
		returnStack.setTagCompound(nbtData);

		returnStack.setStackDisplayName(Integer.toString(nbtData
				.getInteger("Torches")));
		System.out.printf("Set torch nbt to %d\n",
				nbtData.getInteger("Torches"));

		// returnStack.getItem().addInformation(returnStack, , toolTip, true);
		return returnStack;
	}

	@Override
	public int getRecipeSize() {
		// TODO Auto-generated method stub
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		// TODO Auto-generated method stub
		return new ItemStack(NOVA.compressedTorch);
	}

}
