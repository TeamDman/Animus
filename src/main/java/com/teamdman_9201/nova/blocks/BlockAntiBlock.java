package com.teamdman_9201.nova.blocks;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-07-30.
 */
public class BlockAntiBlock extends Block {
    public String  toRepl   = "nope.avi";
    public int     spread   = 0;
    @SideOnly(Side.CLIENT)
    private IIcon        tex;
    private EntityPlayer destroyer;
    private ItemStack    toReturn;
//    private boolean decaying = false;

    public BlockAntiBlock() {
        super(Material.dragonEgg);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return tex;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        tex = reg.registerIcon(NOVA.MODID + ":blockAntiBlock");
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, this, 5);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        if (world.getBlock(x, y, z) == NOVA.blockAntiBlock && stack.getTagCompound() != null && stack.getTagCompound().getString("ID") != null) //shit happens man
            ((BlockAntiBlock) world.getBlock(x, y, z)).toRepl = stack.getTagCompound().getString("ID");

    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block me, int meta) {
        spread = 0;
        super.breakBlock(world, x, y, z, me, meta);
        if (toReturn != null) {
            EntityItem drop = new EntityItem(world, destroyer.posX, destroyer.posY, destroyer.posZ);
            drop.setEntityItemStack(toReturn);
            world.spawnEntityInWorld(drop);
        }
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        world.playSound(x, y, z, "random.fizz", 10, 1, true);
        destroyer = player;
        world.setBlockMetadataWithNotify(x,y,z,1,1);
        updateTick(world, x, y, z, world.rand);

        EntityItem drop = new EntityItem(world, destroyer.posX, destroyer.posY, destroyer.posZ);
        drop.setEntityItemStack(new ItemStack(NOVA.blockAntiBlock));
        world.spawnEntityInWorld(drop);
        return false;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rnd) {
        boolean decaying = world.getBlockMetadata(x,y,z) == 1;
        System.out.printf("ticking to world with decay [%s],spread [%d] and repl [%s]\n",decaying?"true":"false", spread, toRepl);
        if (spread > 255 && !decaying)
            return;
        for (int ox = -1; ox < 2; ox += 1) {
            for (int oy = -1; oy < 2; oy += 1) {
                for (int oz = -1; oz < 2; oz += 1) {
                    Block adj = world.getBlock(x + ox, y + oy, z + oz);
                    if (decaying) {
                        if (adj == NOVA.blockAntiBlock) {
                            ((BlockAntiBlock) adj).setData(destroyer);
                            world.setBlockMetadataWithNotify(x + ox, y + oy, z + oz, 1, 1);
//                            ((BlockAntiBlock) adj).setData(true);
                            world.scheduleBlockUpdate(x + ox, y + oy, z + oz, adj, 5);
                        }
                    } else {
                        if (adj.getUnlocalizedName().equals(toRepl) && adj.getBlockHardness(world,x,y,z) != -1) {
                            world.setBlock(x + ox, y + oy, z + oz, NOVA.blockAntiBlock);
                            ((BlockAntiBlock) world.getBlock(x + ox, y + oy, z + oz)).setData(new ItemStack(adj), toRepl, spread + 1);
                        }
                    }
                }
            }
        }
        if (decaying)
            world.setBlockToAir(x,y,z);
        setTickRandomly(false);
    }

    public void setData(ItemStack give, String replace, int spreaded) {
        toReturn = give;
        toRepl = replace;
        spread = spreaded;
    }
//    public void setData(boolean decay) {
//        decaying = decay;
//    }

    public void setData(EntityPlayer player) {
        destroyer = player;
    }
}

