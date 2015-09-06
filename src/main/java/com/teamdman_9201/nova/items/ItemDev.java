package com.teamdman_9201.nova.items;

import WayofTime.alchemicalWizardry.api.rituals.IRitualStone;
import WayofTime.alchemicalWizardry.common.block.BlockMasterStone;
import com.teamdman_9201.nova.NOVA;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

public class ItemDev extends Item {
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List data, boolean wut) {
        data.add("Is Dev Env: " + NOVA.isDevEnv);
        data.add("Damage: infinite");
        data.add("Sneak damage: AOE kill");
        data.add("Right click mob: voids mob");
        data.add("Sneak right click mob: AOE voids mobs");
        data.add("Right click MRS: prints ritual design code");
        data.add("Right click log: AOE voids logs");
        data.add("Sneak right click block: drops block as item");
    }

    @Override
    public boolean onItemUseFirst(ItemStack devItem, EntityPlayer player, World world, int x, int y, int z, int metaa, float dx, float dy, float dz) {
        if (world.getBlock(x,y,z) instanceof BlockMasterStone && world.isRemote) {
            for (int xPos = -50; xPos < 50; ++xPos) {
                for (int yPos = 0; yPos < 256; yPos++) {
                    for (int zPos = -50; zPos < 50; ++zPos) {
                        if (xPos == 0 && yPos == y-1 && zPos == 0)
                            continue;
                        if (world.getBlock(x + xPos, yPos, z + zPos) instanceof IRitualStone) {
                            String type;
                            int meta = world.getBlockMetadata(x + xPos, yPos, z + zPos);
                            switch (meta) {
                                case 0:
                                    type = "BLANK";
                                    break;
                                case 1:
                                    type = "WATER";
                                    break;
                                case 2:
                                    type = "FIRE";
                                    break;
                                case 3:
                                    type = "EARTH";
                                    break;
                                case 4:
                                    type = "AIR";
                                    break;
                                case 5:
                                    type = "DUSK";
                                    break;
                                default:
                                    type = meta + "";
                                    break;
                            }
                            System.out.println("ritualBlocks.add(new RitualComponent(" + xPos + "," +
                                    "" + (yPos - y) + "," + zPos + ", RitualComponent." + type + "));");
                        }
                    }
                }
            }
            return false;
        } else if (world.getBlock(x,y,z) == Blocks.log) {
            for (int posX = -50; posX < 50; posX++) {
                for (int posY = -50; posY < 50; posY++) {
                    for (int posZ = -50; posZ < 50; posZ++) {
                        if (world.getBlock(x + posX, y + posY, z + posZ) == Blocks.log) {
                            world.setBlockToAir(x + posX, y + posY, z + posZ);
                        }
                    }
                }
            }
            return false;
        }
        if (player.isSneaking()) {
            world.playSound(x, y, z, "random.fizz", 10, 1, true);
            if (!world.isRemote) {
                EntityItem drop = new EntityItem(world, x, y, z, new ItemStack(world.getBlock(x, y, z)));
                world.spawnEntityInWorld(drop);
            }
            world.setBlockToAir(x, y, z);
            return false;
        }
        return  false;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase hit, EntityLivingBase hitter) {
        DamageSource ultimate = new DamageSource("NOVA.absolute").setDamageAllowedInCreativeMode().setDamageBypassesArmor().setDamageIsAbsolute();
        hit.attackEntityFrom(ultimate, Integer.MAX_VALUE);
        if (hitter.isSneaking()) {
            int d0 = 50;
            AxisAlignedBB region = AxisAlignedBB.getBoundingBox(hitter.posX - 1, hitter.posY - 2, hitter.posZ - 1, hitter.posX + 1, hitter.posY + 2, hitter.posZ + 1).expand(d0, d0, d0);
            List<EntityLivingBase> list = hitter.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, region);
            for (EntityLivingBase mob : list) {
                if (mob != hitter) {
                    mob.attackEntityFrom(ultimate,Integer.MAX_VALUE);
                }
            }
        }
        return false;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase mob) {
        mob.setDead();
        if (player.isSneaking()) {
            int d0 = 50;
            AxisAlignedBB region = AxisAlignedBB.getBoundingBox(player.posX - 1, player.posY - 2, player.posZ - 1, player.posX + 1, player.posY + 2, player.posZ + 1).expand(d0, d0, d0);
            List<EntityLivingBase> list = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, region);
            for (EntityLivingBase mobb : list) {
                if (mobb != player) {
                    mobb.setDead();
                }
            }
        }
        return true;
    }
}
