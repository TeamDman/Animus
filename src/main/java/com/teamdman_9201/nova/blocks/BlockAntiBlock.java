package com.teamdman_9201.nova.blocks;

import com.teamdman_9201.nova.NOVA;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

/**
 * Created by TeamDman on 2015-07-30.
 */
public class BlockAntiBlock extends Block {
    public static int maxSpread = 512;
    public        int toRepl    = -1;
    public        int spread    = 0;
    @SideOnly(Side.CLIENT)
    private IIcon        tex;

    public BlockAntiBlock() {
        super(Material.dragonEgg);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block me, int meta) {
        spread = 0;
        super.breakBlock(world, x, y, z, me, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return tex;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        world.scheduleBlockUpdate(x, y, z, this, 5);
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        tex = reg.registerIcon(NOVA.MODID + ":blockAntiBlock");
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        world.playSound(x, y, z, "random.fizz", 10, 1, true);
        world.setBlockMetadataWithNotify(x, y, z, 1, 1);
        updateTick(world, x, y, z, world.rand);
        return false;
    }

    public void setData(int replace, int spreaded) {
        toRepl = replace;
        spread = spreaded;
    }


    @Override
    public void updateTick(World world, int x, int y, int z, Random rnd) {
        boolean decaying = world.getBlockMetadata(x, y, z) == 1;
        if (spread > maxSpread && !decaying)
            return;
        for (int ox = -1; ox < 2; ox += 1) {
            for (int oy = -1; oy < 2; oy += 1) {
                for (int oz = -1; oz < 2; oz += 1) {
                    Block adj = world.getBlock(x + ox, y + oy, z + oz);
                    if (decaying) {
                        if (adj == NOVA.blockAntiBlock) {
                            world.setBlockMetadataWithNotify(x + ox, y + oy, z + oz, 1, 1);
                            world.scheduleBlockUpdate(x + ox, y + oy, z + oz, adj, 5);
                        }
                    } else {
                        if (Block.getIdFromBlock(adj) == toRepl && adj.getBlockHardness(world, x, y, z) != -1) {
                            world.setBlock(x + ox, y + oy, z + oz, NOVA.blockAntiBlock);
                            ((BlockAntiBlock) world.getBlock(x + ox, y + oy, z + oz)).setData(toRepl, spread + 1);
                        }
                    }
                }
            }
        }
        if (decaying)
            world.setBlockToAir(x, y, z);
        setTickRandomly(false);
    }
}

