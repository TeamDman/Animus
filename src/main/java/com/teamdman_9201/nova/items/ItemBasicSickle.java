package com.teamdman_9201.nova.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;

import java.util.List;

import WayofTime.alchemicalWizardry.common.spell.complex.effect.SpellHelper;

public class ItemBasicSickle extends ItemSword {
    float attackDamage;
    Item.ToolMaterial mat;
    public ItemBasicSickle(Item.ToolMaterial material) {
        super(material);
        mat = material;
        attackDamage = 2.0F + material.getDamageVsEntity();
    }

    @Override
    public Multimap getItemAttributeModifiers() {
        Multimap multimap = HashMultimap.create(); //super.getItemAttributeModifiers();
        multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new
                AttributeModifier(field_111210_e, "Weapon modifier", (double) this.attackDamage,
                0));
        return multimap;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase hit, EntityLivingBase attacker) {
        double        x      = hit.posX;
        double        y      = hit.posY;
        double        z      = hit.posZ;
        int           d0     = (mat.getHarvestLevel()+1)*2;
        AxisAlignedBB region = AxisAlignedBB.getBoundingBox(x, y, z, x, y, z).expand(d0, d0, d0);
        List<EntityLivingBase> entities = hit.worldObj.getEntitiesWithinAABB(EntityLivingBase
                .class, region);
        if (entities == null || entities.isEmpty())
            return false;
        for (EntityLivingBase target : entities) {
            if (target instanceof EntityPlayer)
                continue;
            if (attacker == null || target == null || attacker.worldObj.isRemote || (attacker
                    instanceof EntityPlayer && SpellHelper.isFakePlayer(attacker.worldObj,
                    (EntityPlayer) attacker)))
                continue;
            target.attackEntityFrom(DamageSource.causeMobDamage(attacker), attackDamage);
            stack.damageItem(1,attacker);
        }
        return false;
    }
}
