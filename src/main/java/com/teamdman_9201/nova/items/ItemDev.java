package com.teamdman_9201.nova.items;

import WayofTime.alchemicalWizardry.ModBlocks;
import WayofTime.alchemicalWizardry.api.rituals.IRitualStone;
import WayofTime.alchemicalWizardry.common.block.BlockAltar;
import WayofTime.alchemicalWizardry.common.block.BlockMasterStone;
import WayofTime.alchemicalWizardry.common.tileEntity.TEAltar;
import com.teamdman_9201.nova.NOVA;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemDev extends Item {
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List data, boolean wut) {
        data.add("Is Dev Env: " + NOVA.isDevEnv);
    }

    @Override
    public boolean onItemUseFirst(ItemStack devItem, EntityPlayer player, World world, int x, int y, int z, int metaa, float dx, float dy, float dz) {
        if (world.isRemote)
            return false;
        if (world.getBlock(x,y,z) instanceof BlockMasterStone) {
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
            return true;
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
            return true;
        } else if (world.getBlock(x,y,z) == ModBlocks.blockAltar) {
            BlockAltar altar = (BlockAltar) world.getBlock(x,y,z);
            TEAltar te = (TEAltar) world.getTileEntity(x,y,z);
            System.out.printf("\nTier %d\n",te.getTier());
            for (int posX = -50;posX<50;posX++) {
                for (int posY = -50;posY<50;posY++) {
                    for (int posZ = -50; posZ<50;posZ++) {
                        Block b = world.getBlock(x+posX,y+posY,z+posZ);
                        if (b != Blocks.dirt && b!=Blocks.grass && b!=ModBlocks.blockAltar && b != Blocks.air && b!=Blocks.bedrock)
                            System.out.printf("tier%d.put(new pos(%d,%d,%d),Blocks.%s;\n",te.getTier(),posX,posY,posZ,b.getLocalizedName());
                    }
                }
            }
            return true;
        }
        if (player.isSneaking()) {
            EntityItem drop = new EntityItem(world,x,y,z,new ItemStack(world.getBlock(x,y,z)));
            world.spawnEntityInWorld(drop);
            world.setBlockToAir(x,y,z);
            world.playSound(x,y,z,"random.fizz",10,1,true);
            return true;
        }
    return  false;
    }
}
