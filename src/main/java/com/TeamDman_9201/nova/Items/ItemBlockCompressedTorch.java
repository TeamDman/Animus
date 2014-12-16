package com.TeamDman_9201.nova.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.TeamDman_9201.nova.NOVA;
import com.TeamDman_9201.nova.Tiles.TileCompressedTorch;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockCompressedTorch extends ItemBlock {
	// Cazzar> Teamy, create another class, which extends that, and register it
	// with the block.
	Block	cTorch;

	public ItemBlockCompressedTorch(Block arg) {
		super(arg);
		cTorch = arg;
	}

	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		list.add("Torches: " + itemStack.getTagCompound().getLong("Torches"));
		// list.add(EnumChatFormatting.GREEN + "code: " + code);
	}
}