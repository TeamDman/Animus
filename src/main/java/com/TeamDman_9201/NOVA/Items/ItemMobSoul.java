package com.teamdman_9201.nova.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by TeamDman on 2015-06-10.
 */
public class ItemMobSoul extends Item {
    @Override
    public boolean onItemUse(ItemStack soul, EntityPlayer player, World world, int x, int y, int
            z, int side, float xOffset, float yOffset, float zOffset) {
        if (soul.getTagCompound() == null || player == null)
            return false;
        if (world.isRemote)
            return true;
        Entity         mob;
        NBTTagCompound root = soul.stackTagCompound;
        if (root.hasKey("id")) {
            String entityId = root.getString("id");
            mob = EntityList.createEntityByName(entityId, world);
        } else {
            mob = EntityList.createEntityFromNBT(root.getCompoundTag("MobData"), world);
        }
        if (mob == null)
            return true;
        mob.readFromNBT(root.getCompoundTag("MobData"));
        Block  block  = world.getBlock(x, y, z);
        double spawnX = x + Facing.offsetsXForSide[side] + 0.5;
        double spawnY = y + Facing.offsetsYForSide[side];
        double spawnZ = z + Facing.offsetsZForSide[side] + 0.5;
        if (side == ForgeDirection.UP.ordinal() && (block instanceof BlockFence || block
                instanceof BlockWall)) {
            spawnY += 0.5;
        }
        mob.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() * 360.0F, 0);
        boolean spaceClear = world.checkNoEntityCollision(mob.boundingBox) && world
                .getCollidingBoundingBoxes(mob, mob.boundingBox).isEmpty();
        if (!spaceClear)
            return false;

        if (root.hasKey("name") && mob instanceof EntityLiving)
            ((EntityLiving) mob).setCustomNameTag(root.getString("name"));

        world.spawnEntityInWorld(mob);

        if (mob instanceof EntityLiving)
            ((EntityLiving) mob).playLivingSound();

        Entity riddenByEntity = mob.riddenByEntity;
        while (riddenByEntity != null) {
            riddenByEntity.setLocationAndAngles(spawnX, spawnY, spawnZ, world.rand.nextFloat() *
                    360.0F, 0.0F);
            world.spawnEntityInWorld(riddenByEntity);
            if (riddenByEntity instanceof EntityLiving) {
                ((EntityLiving) riddenByEntity).playLivingSound();
            }
            riddenByEntity = riddenByEntity.riddenByEntity;
        }
        soul.stackSize = 0;
        return true;
    }
}
