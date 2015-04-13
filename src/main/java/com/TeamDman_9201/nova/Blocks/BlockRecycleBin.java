package com.TeamDman_9201.nova.Blocks;

import com.TeamDman_9201.nova.NOVA;
import com.TeamDman_9201.nova.Tiles.TileCobblizer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-04-04.
 */
public class BlockRecycleBin extends BlockContainer {
  private final Random random = new Random();
  @SideOnly(Side.CLIENT)
  private IIcon field_150029_a;
  @SideOnly(Side.CLIENT)
  private IIcon field_150028_b;
  @SideOnly(Side.CLIENT)
  private IIcon field_150030_M;
  @SideOnly(Side.CLIENT)
  private IIcon blockIcon;

  public BlockRecycleBin() {
    super(Material.iron);
  }

  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister iconRegister) {
    this.blockIcon = iconRegister.registerIcon(NOVA.MODID + ":" + "blockBrickFurnaceSide");
  }

//  public boolean isOpaqueCube() {
//    return true;
//  }

//  public int getRenderType() {
//    return 24;
//  }

//  public boolean renderAsNormalBlock() {
//    return false;
//  }


  @SideOnly(Side.CLIENT)
  public IIcon getIcon(int side, int metadata) {
    return blockIcon;
  }

  @SideOnly(Side.CLIENT)
  public Item getItem(World world, int x, int y, int z) {
    return Item.getItemFromBlock(NOVA.blockRecycleBin);
  }

  @Override
  public TileEntity createNewTileEntity(World var1, int var2) {
    return new TileCobblizer();
  }

  public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
    return Item.getItemFromBlock(NOVA.blockRecycleBin);
  }

  public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                  int p_149727_6_, float p_149727_7_, float p_149727_8_,
                                  float p_149727_9_) {
    if (world.isRemote) {
      return true;
    } else {
        player.openGui(NOVA.instance, NOVA.guiRecycleBin, world, x, y, z);
      return true;
    }
  }

  public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_) {
      TileCobblizer tile = (TileCobblizer) world.getTileEntity(x, y, z);

      if (tile != null) {
        for (int i1 = 0; i1 < tile.getSizeInventory(); ++i1) {
          ItemStack itemstack = tile.getStackInSlot(i1);

          if (itemstack != null) {
            float f = random.nextFloat() * 0.8F + 0.1F;
            float f1 = random.nextFloat() * 0.8F + 0.1F;
            float f2 = random.nextFloat() * 0.8F + 0.1F;

            while (itemstack.stackSize > 0) {
              int j1 = random.nextInt(21) + 10;

              if (j1 > itemstack.stackSize) {
                j1 = itemstack.stackSize;
              }

              itemstack.stackSize -= j1;
              EntityItem
                  entityitem =
                  new EntityItem(world, (double) ((float) x + f), (double) ((float) y + f1),
                                 (double) ((float) z + f2),
                                 new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));

              if (itemstack.hasTagCompound()) {
                entityitem.getEntityItem()
                    .setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
              }

              float f3 = 0.05F;
              entityitem.motionX = (double) ((float) random.nextGaussian() * f3);
              entityitem.motionY = (double) ((float) random.nextGaussian() * f3 + 0.2F);
              entityitem.motionZ = (double) ((float) random.nextGaussian() * f3);
              world.spawnEntityInWorld(entityitem);
            }
          }
        }

        world.func_147453_f(x, y, z, block);
      }
    super.breakBlock(world, x, y, z, block, p_149749_6_);
  }

  public boolean hasComparatorInputOverride() {
    return true;
  }

  public int getComparatorInputOverride(World world, int x, int y, int z, int p_149736_5_) {
    return Container.calcRedstoneFromInventory((IInventory) world.getTileEntity(x, y, z));
  }
}
