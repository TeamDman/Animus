package com.TeamDman.nova.Blocks;

import com.TeamDman.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCoalDiamondOre extends Block {

  @SideOnly(Side.CLIENT)
  private IIcon blockIcon;
  Random rnd = new Random();

  public BlockCoalDiamondOre() {
    super(Material.rock);
  }

  @Override
  public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
    return rnd.nextInt(2) == 1 ? Items.diamond : NOVA.itemSuperCoal;
  }

  @Override
  public int quantityDropped(Random rnd) {
    return rnd.nextInt(4) + 1;
  }

  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister iconRegister) {
    this.blockIcon = iconRegister.registerIcon(NOVA.MODID + ":" + "blockCoalDiamondOre");
  }

  @SideOnly(Side.CLIENT)
  public IIcon getIcon(int side, int metadata) {
    return this.blockIcon;
  }
}
