package com.teamdman_9201.nova.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;

public class ItemUnstableCoal extends Item {

    public ItemUnstableCoal() {
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer me) {
        double             x        = me.posX;
        double             y        = me.posY;
        double             z        = me.posZ;
        Explosion          boom     = world.createExplosion(null, x, y, z, 100, true);
        DamageSource       ultimate = new DamageSource("NOVA.absolute").setDamageAllowedInCreativeMode().setDamageBypassesArmor().setDamageIsAbsolute();
        int                d0       = 5;
        AxisAlignedBB      region   = AxisAlignedBB.getBoundingBox( x-1, y-2, z-1, x + 1, y + 2, z + 1).expand(d0, d0, d0);
        List<EntityPlayer> list     = world.getEntitiesWithinAABB(EntityPlayer.class, region);
        for (EntityPlayer hit : list) {
            System.out.printf("Killing %s\n", hit.getDisplayName());
            hit.attackEntityFrom(ultimate, Integer.MAX_VALUE);
        }
        return stack;
    }

}
