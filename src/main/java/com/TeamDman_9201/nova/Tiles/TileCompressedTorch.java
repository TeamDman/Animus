package com.TeamDman_9201.nova.Tiles;

import java.util.List;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class TileCompressedTorch extends TileEntity {
	long	torches	= 0;

	public long getTorches() {
		return torches;
	}

	public void setTorches(long val) {
		this.torches = val;
	}

	public void checkTags(NBTTagCompound nbt) {
	}

	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.torches = tagCompound.getLong("Torches");
	}

	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setLong("Torches", torches);
	}
}
