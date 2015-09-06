package com.teamdman_9201.nova.items.sigils;

import WayofTime.alchemicalWizardry.api.items.interfaces.ArmourUpgrade;
import WayofTime.alchemicalWizardry.common.items.EnergyItems;
import com.teamdman_9201.nova.NOVA;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilOfFastBuilder extends EnergyItems implements ArmourUpgrade {
    @SideOnly(Side.CLIENT)
    private IIcon activeIcon;
    @SideOnly(Side.CLIENT)
    private IIcon passiveIcon;

    public ItemSigilOfFastBuilder() {
        super();
        this.maxStackSize = 1;
        setEnergyUsed(1);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List data, boolean wat) {
        if (!(stack.getTagCompound() == null))
            data.add(StatCollector.translateToLocal("tooltip.owner.currentowner") + stack.getTagCompound().getString("ownerName"));
        if (stack.getTagCompound().getBoolean("isActive")) {
            data.add(StatCollector.translateToLocal("tooltip.sigil.state.activated"));
        } else {
            data.add(StatCollector.translateToLocal("tooltip.sigil.state.deactivated"));
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        this.activeIcon = iconRegister.registerIcon(NOVA.MODID + ":itemSigilOfFastBuilderActive");
        this.passiveIcon = iconRegister.registerIcon(NOVA.MODID + ":itemSigilOfFastBuilderDeactivated");
    }

    @Override
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        if (stack.getTagCompound() == null)
            stack.setTagCompound(new NBTTagCompound());
        NBTTagCompound tag = stack.getTagCompound();

        if (tag.getBoolean("isActive")) {
            return this.activeIcon;
        } else {
            return this.passiveIcon;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int par1) {
        if (par1 == 1) {
            return this.activeIcon;
        } else {
            return this.passiveIcon;
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack sigil, World world, EntityPlayer player) {
        if (sigil.getTagCompound() == null)
            EnergyItems.checkAndSetItemOwner(sigil, player);

        NBTTagCompound tag = sigil.getTagCompound();
        tag.setBoolean("isActive", !(tag.getBoolean("isActive")));
        sigil.setItemDamage(tag.getBoolean("isActive") ? 1 : 0);
        return sigil;
    }

    @Override
    public int getEnergyForTenSeconds() {
        return 50;
    }

    @Override
    public boolean isUpgrade() {
        return true;
    }

    @Override
    //    @SideOnly(Side.CLIENT)
    public void onUpdate(ItemStack stack, World world, Entity ent, int meta, boolean wat) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().getBoolean("isActive"))
            return;
        if (!(ent instanceof EntityPlayer))
            return;
        if (!EnergyItems.syphonBatteries(stack, (EntityPlayer) ent, getEnergyUsed()))
            return;
        if (!((EntityPlayer) ent).capabilities.isCreativeMode)
            if (((EntityPlayer) ent).getHeldItem() == null || Block.getBlockFromItem(((EntityPlayer) ent).getHeldItem().getItem()) == Blocks.air)
                return;
        ItemSigilOfFastBuilder.removeDelay();
    }

    @Override
    public void onArmourUpdate(World world, EntityPlayer player, ItemStack thisItemStack) {
        if (!(player).capabilities.isCreativeMode)
            if ((player).getHeldItem() == null || Block.getBlockFromItem((player).getHeldItem().getItem()) == Blocks.air)
                return;

        ItemSigilOfFastBuilder.removeDelay();
    }

    public static void removeDelay() {
        try {
            Field delay = Minecraft.class.getDeclaredField("rightClickDelayTimer");
            delay.setAccessible(true);
            try {
                delay.set(Minecraft.getMinecraft(), 0);
            } catch (IllegalAccessException nsfe) {
                throw new RuntimeException(nsfe);
            }
        } catch (NoSuchFieldException nsfe) {
            throw new RuntimeException(nsfe);
        }
    }


}
