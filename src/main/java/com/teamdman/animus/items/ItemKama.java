package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TeamDman on 9/18/2016.
 */
public class ItemKama extends ItemSword implements IVariantProvider {
    float attackDamage;
    Item.ToolMaterial mat;

    public ItemKama(Item.ToolMaterial material) {
        super(material);
        mat = material;
        attackDamage = 2.0F + material.getDamageVsEntity();
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase hit, EntityLivingBase attacker) {
        double x = hit.posX;
        double y = hit.posY;
        double z = hit.posZ;
        int d0 = (mat.getHarvestLevel() + 1) * 2;
        AxisAlignedBB region = new AxisAlignedBB(x, y, z, x, y, z).expand(d0, d0, d0);
        List<EntityLivingBase> entities = hit.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, region);
        if (entities == null || entities.isEmpty())
            return false;
        for (EntityLivingBase target : entities) {
            if (target instanceof EntityPlayer)
                continue;
            if (attacker == null || target == null || attacker.worldObj.isRemote)
                continue;
            target.attackEntityFrom(DamageSource.causeMobDamage(attacker), attackDamage);
            stack.damageItem(1, attacker);
        }
        return false;
    }

    @Override
    public List<Pair<Integer, String>> getVariants()
    {
        List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
        ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
        return ret;
    }
}
