package com.teamdman_9201.nova.items.sigils;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import WayofTime.alchemicalWizardry.common.items.EnergyItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-04-19.
 */
public class ItemSigilOfTransposition extends EnergyItems {
    @SideOnly(Side.CLIENT)
    IIcon icon;
    Block picked;
    int   meta;
    int[] pos = new int[3];
    TileEntity     tile;
    NBTTagCompound inv;
    Random rnd = new Random();
    public static Boolean canMoveTiles = true;

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
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
    icon = reg.registerIcon(NOVA.MODID + ":itemSigilOfTransposition");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_) {
        return icon;
    }

    @Override
    public boolean onItemUseFirst(ItemStack sigil, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz) {
        if (world.isRemote)
            return false;
        if (picked == null && world.getBlock(x,y,z).getBlockHardness(world,x,y,z) != -1) {
            picked = world.getBlock(x, y, z);
            meta = world.getBlockMetadata(x, y, z);
            pos[0] = x;
            pos[1] = y;
            pos[2] = z;
            tile = world.getTileEntity(x, y, z);
            inv = new NBTTagCompound();
            if (tile != null && !canMoveTiles)
                picked = null;
        } else if (picked != null) {
            if (!EnergyItems.syphonBatteries(sigil, player, getEnergyUsed()*(tile==null?1:5)))
                return false;
            if (tile != null)
                tile.writeToNBT(inv);
            y++;
            inv.setInteger("x", x);
            inv.setInteger("y", y);
            inv.setInteger("z", z);
            world.setBlock(x, y, z, picked, meta, 1);
            if (world.getTileEntity(x,y,z)!=null)
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
