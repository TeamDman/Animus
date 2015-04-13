package com.TeamDman_9201.nova.Blocks;

import com.TeamDman_9201.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCoalDiamondOre extends Block {

  @SideOnly(Side.CLIENT)
  private IIcon blockIcon;

  public BlockCoalDiamondOre() {
    super(Material.rock);
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
