package com.teamdman_9201.nova.items;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

import WayofTime.alchemicalWizardry.common.IDemon;
import WayofTime.alchemicalWizardry.common.demonVillage.demonHoard.demon.IHoardDemon;
import WayofTime.alchemicalWizardry.common.items.DaggerOfSacrifice;
import WayofTime.alchemicalWizardry.common.spell.complex.effect.SpellHelper;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBoundSickle extends DaggerOfSacrifice {
    private float weaponDamage;
    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public ItemBoundSickle() {
        super();
        setEnergyUsed(100);
        this.maxStackSize = 1;
        weaponDamage = 2.0F;
    }

    @Override
    public boolean findAndFillAltar(World world, EntityLivingBase sacrifice, int amount) {
        int     posX        = (int) Math.round(sacrifice.posX - 0.5f);
        int     posY        = (int) sacrifice.posY;
        int     posZ        = (int) Math.round(sacrifice.posZ - 0.5f);
        TEAltar altarEntity = this.getAltar(world, posX, posY, posZ);

        if (altarEntity == null) {
            return false;
        }
        if (altarEntity.getCurrentBlood() + amount > altarEntity.getCapacity())
            return false;
        altarEntity.sacrificialDaggerCall(amount, true);
        altarEntity.startCycle();
        return true;
    }

    @Override
    public TEAltar getAltar(World world, int x, int y, int z) {
        TileEntity tileEntity = null;
        int        radius     = 5;
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                for (int k = -radius; k <= radius; k++) {
                    tileEntity = world.getTileEntity(i + x, k + y, j + z);
                    if (tileEntity instanceof TEAltar)
                        return (TEAltar) tileEntity;
                }
            }
        }
        return null;
    }

    @Override
    public IIcon getIconFromDamage(int p_77617_1_) {
        return icon;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase entity_, EntityLivingBase player) {
        double        x      = entity_.posX;
        double        y      = entity_.posY;
        double        z      = entity_.posZ;
        int           d0     = 10;
        AxisAlignedBB region = AxisAlignedBB.getBoundingBox(x, y, z, x, y, z).expand(d0, d0, d0);
        List<EntityLivingBase> entities = entity_.worldObj.getEntitiesWithinAABB(EntityLivingBase
                .class, region);
        if (entities == null || entities.isEmpty())
            return false;
        for (EntityLivingBase entity : entities) {
            if (entity instanceof EntityPlayer)
                continue;
            if (player == null || entity == null || player.worldObj.isRemote || (player
                    instanceof EntityPlayer && SpellHelper.isFakePlayer(player.worldObj,
                    (EntityPlayer) player)))
                continue;
            if (entity instanceof IHoardDemon || entity instanceof EntityWither || entity
                    instanceof EntityDragon || entity instanceof EntityPlayer || entity
                    instanceof IBossDisplayData || entity.isDead || entity.getHealth() < 0.5f)
                continue;

            if (entity instanceof IDemon) {
                ((IDemon) entity).setDropCrystal(false);
                this.findAndNotifyAltarOfDemon(entity.worldObj, entity);
            }

            int blood = 500;
            if (entity instanceof EntityVillager)
                blood = 2000;
            if (entity instanceof EntitySlime)
                blood = 150;
            if (entity instanceof EntityEnderman)
                blood = 200;
            if (entity instanceof EntityAnimal)
                blood = 250;
            if (entity.isChild())
                blood /= 2;


            if (findAndFillAltar(entity.worldObj, entity, blood)) {
                double posX = entity.posX;
                double posY = entity.posY;
                double posZ = entity.posZ;
                for (int i = 0; i < 8; i++) {
                    SpellHelper.sendIndexedParticleToAllAround(entity.worldObj, posX, posY, posZ,
                            20, entity.worldObj.provider.dimensionId, 1, posX, posY, posZ);
                }
                entity.setHealth(-1);
                entity.onDeath(DamageSource.generic);
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(NOVA.MODID + ":itemBoundSickle");
    }
}