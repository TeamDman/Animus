package com.TeamDman.nova.Items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by TeamDman on 2015-04-19.
 */
public class ItemTransportalizer extends Item {

  Block picked;
  int   meta;
  int[] pos = new int[3];
  TileEntity     tile;
  NBTTagCompound inv;
  Random rnd = new Random();

  @Override
  public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y,
                                int z, int side, float px, float py, float pz) {
    if (world.isRemote) {
      return false;
    }
    if (picked == null) {
      picked = world.getBlock(x, y, z);
      meta = world.getBlockMetadata(x, y, z);
      pos[0] = x;
      pos[1] = y;
      pos[2] = z;
      tile = world.getTileEntity(x, y, z);
      inv = new NBTTagCompound();
    } else if (world.getBlock(x, y, z) == Blocks.diamond_block) {
      if (tile != null) {
        tile.writeToNBT(inv);
      }
      if (rnd.nextInt(2) == 1) {
        world.getBlock(x, y, z).dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
      }
      inv.setInteger("x", x);
      inv.setInteger("y", y);
      inv.setInteger("z", z);
      world.setBlock(x, y, z, picked, meta, 1 + 2);
      try {
        world.removeTileEntity(pos[0], pos[1], pos[2]);
        world.setBlockToAir(pos[0], pos[1], pos[2]);
      } catch (Exception e) {
        e.printStackTrace();
      }
      picked = null;
    } else {
      player.addChatComponentMessage(new ChatComponentText(
          "You must target a diamond block as the destination. It may be consumed."));
    }
    return true;
  }

}
