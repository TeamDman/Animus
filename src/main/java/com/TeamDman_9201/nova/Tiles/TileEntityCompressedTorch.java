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

public class TileEntityCompressedTorch extends TileEntity {
	int	torches	= 0;

	public int getTorches() {
		return torches;
	}

	public void setTorches(int val) {
		this.torches = val;
		System.out.println("Torchess set to " + val);
	}

	public void checkTags(NBTTagCompound nbt) {
		System.out.println("Checking tags?");
	}

	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		this.torches = tagCompound.getInteger("Torches");
		System.out.println("READ" + torches);
	}

	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("Torches", torches);
		System.out.println("WRITE" + torches);
	}

//	public void onUpdate(ItemStack stack, World world, Entity entity, int par4,
//			boolean par5) {
//		stack.stackTagCompound.setCompoundTag("display", new NBTTagCompound());
//		NBTTagList lore = new NBTTagList("Lore");
//		NBTTagCompound tag = stack.stackTagCompound.getCompoundTag("display");
//		lore.appendTag(new NBTTagString("lel"));
//		tag.setTag("Lore", lore);
//		tag.setCompoundTag("display", tag);
//	}
}
