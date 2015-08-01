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

    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int meta, float ox, float oy, float oz) {
        Explosion boom = world.createExplosion(null, x, y, z, 100, true);
        DamageSource ultimate = new DamageSource("NOVA.absolute").setDamageAllowedInCreativeMode().setDamageBypassesArmor().setDamageIsAbsolute();
        int           d0     = 5;
        AxisAlignedBB region = AxisAlignedBB.getBoundingBox((double) x, (double) y, (double) z, (double) (x + 1), (double) (y + 2), (double) (z + 1)).expand(d0, d0, d0);
        List<EntityPlayer>          list   = world.getEntitiesWithinAABB(EntityPlayer.class, region);
        for (EntityPlayer hit : list){
            System.out.printf("Killing %s\n",hit.getDisplayName());
            hit.attackEntityFrom(ultimate, Integer.MAX_VALUE);
        }
        return true;
    }
}
