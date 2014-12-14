package com.TeamDman_9201.nova.Tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityCompressedTorch extends TileEntity {
	int torches=0;
	public int getTorches() {
		return torches;
	}
	
	public void checkTags(NBTTagCompound nbt) {
		System.out.println("Checking tags?");
	}
	
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.torches=tagCompound.getInteger("Torches");
		System.out.println("READ");
	}

	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		System.out.println("WRITE");
	}
}
