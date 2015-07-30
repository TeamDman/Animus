package com.teamdman_9201.nova.items.sigils;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

import WayofTime.alchemicalWizardry.common.demonVillage.demonHoard.demon.IHoardDemon;
import WayofTime.alchemicalWizardry.common.items.EnergyItems;
import WayofTime.alchemicalWizardry.common.spell.complex.effect.SpellHelper;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilOfChains extends EnergyItems {
    public ItemSigilOfChains() {
        super();
        this.maxStackSize = 1;
        setEnergyUsed(1000);
    }

    @Override
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List
            par3List, boolean par4) {
        par3List.add(StatCollector.translateToLocal("tooltip.sigilofchains.desc1"));
        par3List.add(StatCollector.translateToLocal("tooltip.sigilofchains.desc2"));

        if (!(par1ItemStack.getTagCompound() == null)) {
            par3List.add(StatCollector.translateToLocal("tooltip.owner.currentowner") + " " +
                    par1ItemStack.getTagCompound().getString("ownerName"));
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack sigil, EntityPlayer player,
                                            EntityLivingBase mob) {
        if (player.worldObj.isRemote)
            return true;
        if (mob instanceof IHoardDemon || mob instanceof EntityWither || mob
                instanceof EntityDragon || mob instanceof EntityPlayer || mob
                instanceof IBossDisplayData || mob.isDead || mob.getHealth() < 0.5f)
            return false;
        if (!EnergyItems.syphonBatteries(sigil, player, getEnergyUsed()))
            return false;
        double x     = player.posX;
        double y     = player.posY;
        double z     = player.posZ;
        World  world = player.worldObj;

        ItemStack      soul    = new ItemStack(NOVA.itemMobSoul);
        NBTTagCompound tag     = new NBTTagCompound();
        NBTTagCompound mobData = new NBTTagCompound();
        for (int i = 0; i < 10; i++) {
            SpellHelper.sendIndexedParticleToAllAround(world, mob.posX, mob.posY, mob.posZ, 20,
                    world.provider.dimensionId, 1, mob.posX, mob.posY, mob.posZ);
        }
        mob.writeToNBT(mobData);
        tag.setString("id", EntityList.getEntityString(mob));
        if (mob instanceof EntityLiving && ((EntityLiving) mob).hasCustomNameTag())
            tag.setString("name", ((EntityLiving) mob).getCustomNameTag());
        tag.setTag("MobData", mobData);
        soul.setTagCompound(tag);
        soul.setStackDisplayName((tag.hasKey("name") ? tag.getString("name") + "'s" : tag
                .getString("id")) + " Soul");
        if (!player.inventory.addItemStackToInventory(soul))
            world.spawnEntityInWorld(new EntityItem(player.worldObj, mob.posX, mob.posY, mob.posZ));
        mob.setDead();
        System.out.println(tag.getString("id"));
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack sigil, World world, EntityPlayer player) {
        if (sigil.getTagCompound() == null)
            EnergyItems.checkAndSetItemOwner(sigil, player);
        return sigil;
    }
}
