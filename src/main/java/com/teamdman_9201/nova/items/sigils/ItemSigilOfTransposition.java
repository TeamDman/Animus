package com.teamdman_9201.nova.items.sigils;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import WayofTime.alchemicalWizardry.common.items.EnergyItems;

/**
 * Created by TeamDman on 2015-04-19.
 */
public class ItemSigilOfTransposition extends EnergyItems {

    Block picked;
    int   meta;
    int[] pos = new int[3];
    TileEntity     tile;
    NBTTagCompound inv;
    Random rnd = new Random();

    public ItemSigilOfTransposition() {
        super();
        this.maxStackSize = 1;
        setEnergyUsed(50000);
    }

    @Override
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
        if (!(par1ItemStack.getTagCompound() == null)) {
            par3List.add(StatCollector.translateToLocal("tooltip.owner.currentowner") + " " +
                    par1ItemStack.getTagCompound().getString("ownerName"));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack sigil, World world, EntityPlayer player) {
        if (sigil.getTagCompound() == null)
            EnergyItems.checkAndSetItemOwner(sigil, player);
        return sigil;
    }

    @Override
    public boolean onItemUseFirst(ItemStack sigil, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz) {
        if (world.isRemote)
            return false;
        if (picked == null) {
            picked = world.getBlock(x, y, z);
            if (picked.getBlockHardness(world,x,y,z) == -1)
                return false;
            meta = world.getBlockMetadata(x, y, z);
            pos[0] = x;
            pos[1] = y;
            pos[2] = z;
            tile = world.getTileEntity(x, y, z);
            inv = new NBTTagCompound();
        } else {
            if (!EnergyItems.syphonBatteries(sigil, player, getEnergyUsed()*(tile==null?1:5)))
                return false;
            if (tile != null)
                tile.writeToNBT(inv);
            y++;
            inv.setInteger("x", x);
            inv.setInteger("y", y);
            inv.setInteger("z", z);
            world.setBlock(x, y, z, picked, meta, 1);
            world.getTileEntity(x,y,z).readFromNBT(inv);
            try {
                world.removeTileEntity(pos[0], pos[1], pos[2]);
                world.setBlockToAir(pos[0], pos[1], pos[2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            picked = null;
//            player.addChatComponentMessage(new ChatComponentText("You must target a diamond block as the destination. It may be consumed."));
        }
        return true;
    }
}
