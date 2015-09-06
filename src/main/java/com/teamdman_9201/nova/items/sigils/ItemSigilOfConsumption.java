package com.teamdman_9201.nova.items.sigils;

import WayofTime.alchemicalWizardry.common.items.EnergyItems;
import com.teamdman_9201.nova.NOVA;
import com.teamdman_9201.nova.blocks.BlockAntiBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilOfConsumption extends EnergyItems {
    public ItemSigilOfConsumption() {
        super();
        this.maxStackSize = 1;
        setEnergyUsed(1000);
    }

    @Override
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List
            par3List, boolean par4) {
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
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int meta, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
        if (world.getBlock(x,y,z).getBlockHardness(world,x,y,z)!=-1) {
            if (!EnergyItems.syphonBatteries(stack, player, getEnergyUsed()))
                return false;
            EnergyItems.syphonBatteries(stack,player,BlockAntiBlock.maxSpread^2/1000);
            int ID = Block.getIdFromBlock(world.getBlock(x, y, z));
            world.setBlock(x,y,z, NOVA.blockAntiBlock);
            ((BlockAntiBlock) world.getBlock(x, y, z)).toRepl = ID;
        }
        return false;
    }

}
