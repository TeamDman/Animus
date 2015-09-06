package com.teamdman_9201.nova.items;

import WayofTime.alchemicalWizardry.ModBlocks;
import WayofTime.alchemicalWizardry.common.block.BloodRune;
import WayofTime.alchemicalWizardry.common.bloodAltarUpgrade.AltarComponent;
import WayofTime.alchemicalWizardry.common.bloodAltarUpgrade.UpgradedAltars;
import WayofTime.alchemicalWizardry.common.spell.complex.effect.SpellHelper;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by TeamDman on 2015-08-30.
 */
public class ItemAltarDiviner extends Item {
    @Override
    public boolean onItemUseFirst(ItemStack item, EntityPlayer player, World world, int x, int y, int z, int meta, float dx, float dy, float dz) {
        if (world.getBlock(x, y, z) == ModBlocks.blockAltar) {
            TEAltar te = (TEAltar) world.getTileEntity(x, y, z);
            te.checkAndSetAltar();
            List<AltarComponent> altarParts = UpgradedAltars.getAltarUpgradeListForTier(te.getTier() + 1);
            if (altarParts == null)
                return false;
            for (AltarComponent comp : altarParts) {
                int wx = x + comp.getX();
                int wy = y + comp.getY();
                int wz = z + comp.getZ();
                if (world.getBlock(wx, wy, wz) == Blocks.air) {
                    Block toPlace = null;
                    int toMeta = -1;
                    if (!player.capabilities.isCreativeMode) {
                        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
                            if (player.inventory.getStackInSlot(slot) == null || player.inventory.getStackInSlot(slot).getItem() == null)
                                continue;
                            if (Block.getBlockFromItem(player.inventory.getStackInSlot(slot).getItem()) == comp.getBlock() || (comp.isBloodRune() || comp.isUpgradeSlot()) && Block.getBlockFromItem(player.inventory.getStackInSlot(slot).getItem()) instanceof BloodRune) {
                                toPlace = Block.getBlockFromItem(player.inventory.getStackInSlot(slot).getItem());
                                toMeta = player.inventory.getStackInSlot(slot).getItemDamage();
                                player.inventory.decrStackSize(slot, 1);
                                break;
                            }
                        }
                        if (toMeta == -1) {
                            player.addChatMessage(new ChatComponentText("You are missing a " + comp.getBlock().getLocalizedName() + "!"));
                            return false;
                        }
                    }
                    if (toPlace == null)
                        toPlace = comp.getBlock();
                    if (toMeta == -1)
                        toMeta = comp.getMetadata();
                    SpellHelper.sendIndexedParticleToAllAround(world, x, y, z, 30, world.provider.dimensionId, 1, x, y, z);
                    world.setBlock(wx, wy, wz, toPlace, toMeta, 3);
                    if (!player.capabilities.isCreativeMode) {
                        break;
                    }
                }
            }
            return false;
        }
        return false;
    }
}