package com.teamdman_9201.nova.items;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-07-31.
 */
public class ItemRedundantOrb extends Item {
    @Override
    public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
        return false;
    }

    public ItemRedundantOrb() {
        setMaxStackSize(1);
    }

    @SideOnly(Side.CLIENT)
    IIcon happy;
    @SideOnly(Side.CLIENT)
    IIcon sad;

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List data, boolean wut) {
        //data.add("why don't you love me?");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack me) {
        return happy;
    }

    @Override
    public void onUpdate(ItemStack item, World world, Entity ent, int meta, boolean wat) {
        if (ent instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) ent;
            for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
                if (player.inventory.getStackInSlot(slot) == null) {
                    player.inventory.setInventorySlotContents(slot, new ItemStack(NOVA.itemRedundantOrb));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        happy = reg.registerIcon(NOVA.MODID + ":itemRedundantOrbHappy");
        sad = reg.registerIcon(NOVA.MODID + ":itemRedundantOrbSad");
    }

}
