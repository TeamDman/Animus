package com.teamdman.animus.world.generation;

import java.util.Random;

import com.teamdman.animus.Animus;
import com.teamdman.animus.registry.AnimusBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public abstract class BloodTreeGenerator extends WorldGenerator {

    public BloodTreeGenerator(boolean doNotify) {
        super(doNotify);
    }
	
    @Override
    public boolean generate(World world, Random rand, BlockPos blockPos) {
        int x = blockPos.getX();
        int retries = blockPos.getY();
        int z = blockPos.getZ();
        for(int c = 0; c < retries; c++) {
            int y = world.getActualHeight() - 1;
            BlockPos loopPos = new BlockPos(x, y, z);
            while(world.isAirBlock(loopPos) && y > 0) {
                y--;
            }

            if(!growTree(world, rand, loopPos.add(0, 1, 0))) {
                retries--;
            }

            x += rand.nextInt(16) - 8;
            z += rand.nextInt(16) - 8;
        }

        return true;
    }
    
    protected static int baseHeight = 8;
    protected static int baseHeightRandomRange = 3;
    public BlockLeaves leaves = AnimusBlocks.BLOCKBLOODLEAVES;
    public BlockLog logs = AnimusBlocks.BLOCKBLOODWOOD;
    public BlockSapling saplings = AnimusBlocks.BLOCKBLOODSAPLING;
    
    
    public boolean growTree(World world, Random rand, BlockPos blockPos) {
        int treeHeight = rand.nextInt(baseHeightRandomRange) + baseHeight;
        int worldHeight = world.getHeight();
        Block block;

        if(blockPos.getY() >= 1 && blockPos.getY() + treeHeight + 1 <= worldHeight) {
            int xOffset;
            int yOffset;
            int zOffset;

            BlockPos basePos = blockPos.add(0, -1, 0);
            IBlockState blockState = world.getBlockState(basePos);
            block = blockState.getBlock();
            int x = blockPos.getX();
            int y = blockPos.getY();
            int z = blockPos.getZ();

            if((block != null && block.canSustainPlant(blockState, world, basePos, EnumFacing.UP,
            		saplings)) && y < worldHeight - treeHeight - 1) {
                for(yOffset = y; yOffset <= y + 1 + treeHeight; ++yOffset) {
                    byte radius = 1;

                    if(yOffset == y) {
                        radius = 0;
                    }

                    if(yOffset >= y + 1 + treeHeight - 3) {
                        radius = 3;
                    }

                    // Check if leaves can be placed
                    if(yOffset >= 0 & yOffset < worldHeight) {
                        for(xOffset = x - radius; xOffset <= x + radius; ++xOffset) {
                            for(zOffset = z - radius; zOffset <= z + radius; ++zOffset) {
                                BlockPos loopPos = new BlockPos(xOffset, yOffset, zOffset);
                                IBlockState loopBlockState = world.getBlockState(loopPos);
                                block = loopBlockState.getBlock();

                                if(block != null && !(block.isLeaves(loopBlockState, world, loopPos) ||
                                        block == Blocks.AIR ||
                                        block.canBeReplacedByLeaves(loopBlockState, world, loopPos))) {
                                    return false;
                                }
                            }
                        }
                    } else {
                        return false;
                    }
                }

                if (block != null) {
                    block.onPlantGrow(blockState, world, basePos, blockPos);

                    // Add leaves
                    for(yOffset = y - 3 + treeHeight; yOffset <= y + treeHeight; ++yOffset) {
                        int var12 = yOffset - (y + treeHeight);
                        int center = 1 - var12 / 2;

                        for(xOffset = x - center; xOffset <= x + center; ++xOffset) {
                            int xPos = xOffset - x;
                            int t = xPos >> 31;
                            xPos = (xPos + t) ^ t;

                            for(zOffset = z - center; zOffset <= z + center; ++zOffset) {
                                int zPos = zOffset - z;
                                zPos = (zPos + (t = zPos >> 31)) ^ t;
                                BlockPos loopPos = new BlockPos(xOffset, yOffset, zOffset);
                                IBlockState loopBlockState = world.getBlockState(loopPos);
    
                                block = loopBlockState.getBlock();
    
                                if(((xPos != center | zPos != center) ||
                                        rand.nextInt(2) != 0 && var12 != 0) &&
                                        (block == null || block.isLeaves(loopBlockState, world, loopPos) ||
                                        block == Blocks.AIR ||
                                        block.canBeReplacedByLeaves(loopBlockState, world, loopPos))) {
                                    this.setBlockAndNotifyAdequately(world, loopPos, leaves.getDefaultState());
                                }
                            }
                        }
                    }

                    // Replace replacable blocks with logs
                    for(yOffset = 0; yOffset < treeHeight; ++yOffset) {
                        BlockPos loopPos = blockPos.add(0, yOffset, 0);
                        IBlockState loopBlockState = world.getBlockState(loopPos);
                        block = loopBlockState.getBlock();

                        if(block == null || block == Blocks.AIR  ||
                                block.isLeaves(loopBlockState, world, loopPos) ||
                                block.isReplaceable(world, loopPos)) {
                            this.setBlockAndNotifyAdequately(world, loopPos,
                                    logs.getDefaultState().withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y));
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
}
