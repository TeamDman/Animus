package com.teamdman_9201.nova.items;

import com.teamdman_9201.nova.NOVA;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.Random;

/**
 * Created by TeamDman on 2015-04-13.
 */
public class ItemBlockSapling extends ItemBlock {

    private static Random rand = new Random();

    public ItemBlockSapling(Block arg) {
        super(NOVA.blockSapling);
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem ent) {
        boolean isDelayOver = ent.age > 60 && ent.delayBeforeCanPickup == 0;
        if (ent.onGround || Math.abs(ent.motionY) < 0.13D) {
            if (!ent.worldObj.isRemote) {
                int landedBlockX = (int) Math.floor(ent.posX);// - 0.5F);
                int landedBlockY = (int) Math.floor(ent.posY);
                int landedBlockZ = (int) Math.floor(ent.posZ);// - 0.5F);

                ItemStack stack = ent.getEntityItem();
                if (isDelayOver) {
                    if (NOVA.blockSapling.canPlaceBlockAt(ent.worldObj, landedBlockX,
                            landedBlockY, landedBlockZ)) {
                        //          if ((ent.worldObj.isAirBlock(landedBlockX, landedBlockY,
                        // landedBlockZ) || ent.worldObj.is)&& (
                        //              ent.worldObj.getBlock(landedBlockX, landedBlockY - 1,
                        // landedBlockZ) == blocks.dirt
                        //              || ent.worldObj.getBlock(landedBlockX, landedBlockY - 1,
                        // landedBlockZ)
                        //                 == blocks.grass)) {

                        ent.worldObj.setBlock(landedBlockX, landedBlockY, landedBlockZ, NOVA
                                .blockSapling);

                        ent.playSound("mob.chicken.plop", 1.0F, (rand.nextFloat() - rand
                                .nextFloat()) * 0.2F + 1.0F);

                        if (stack.stackSize == 1) {
                            ent.setDead();
                        } else {
                            stack.stackSize--;
                        }
                    }
                }
            }
        }
        return false;
    }
}
