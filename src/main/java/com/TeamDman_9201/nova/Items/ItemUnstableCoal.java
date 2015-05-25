package com.TeamDman.nova.Items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemUnstableCoal extends Item {

  public ItemUnstableCoal() {
  }

  public boolean onItemUse(ItemStack par1ItemStack,
                           EntityPlayer par2EntityPlayer, World par3World, int x, int y,
                           int z, int par7, float par8, float par9, float par10) {
    par3World.createExplosion(null, x, y, z, 5, true);
    return true;
  }
}
