// Thanks to bro @ https://github.com/4poc/lloverlay/blob/master/forge_src/cc/apoc/lloverlay/LightLevelOverlayThread.java
package com.TeamDman.nova.Threads;

import com.TeamDman.nova.Tiles.TileLightManipulator;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;

public class ThreadLightManipulator {// extends Thread {
  private static Block[]
      OVERLAY_BLOCKS =
      new Block[]{Blocks.farmland, Blocks.wooden_slab, Blocks.stone_slab, Blocks.glass,
                  Blocks.snow, Blocks.ice, Blocks.glowstone, Blocks.piston, Blocks.piston_extension,
                  Blocks.piston_head, Blocks.daylight_detector,
                  Blocks.leaves, Blocks.carpet                                // carpet
      };
  private TileLightManipulator tileLightManipulator;

  public ThreadLightManipulator(TileLightManipulator tile) {
    tileLightManipulator = tile;
  }

  public boolean isOverlayBlock(Block block) {
    if (block == null) {
      return false;
    }
    for (Block v : OVERLAY_BLOCKS) {
      if (Block.isEqualTo(v, block)) {
        return true;
      }
    }
    return false;
  }

  public boolean correctLight(World world, int x, int y, int z) {
    if (!world.isRemote) {
      // Block block = world.getBlock(x, y, z);
      for (int i = 0; i < 7; i++) {
        ItemStack stack = tileLightManipulator.getStackInSlot(i);
        if (stack != null && stack.stackSize > 0) {
          if (world.getSavedLightValue(EnumSkyBlock.Block, x, y + 1, z) <= 7) {
            if (true) {// block.isOpaqueCube() ||
              // isOverlayBlock(block)) {
//							world.setBlock(x, y + 1, z, Blocks.torch);
              world.setBlock(x, y + 1, z, Block
                  .getBlockFromItem(tileLightManipulator.getStackInSlot(i).getItem()));
              tileLightManipulator.decrStackSize(i, 1);
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public ArrayList<int[]> getBlocksInChunk(World world, boolean useSkyLightLevel, int radius) {
    long tStart = System.currentTimeMillis();
    ArrayList<int[]> chunkContents = new ArrayList<int[]>();
    int startX = tileLightManipulator.xCoord;
    int startY = tileLightManipulator.yCoord;
    int startZ = tileLightManipulator.zCoord;
    // int daChunk = tileLightManipulator
    int startChunkX = startX >> 4;
    int startChunkZ = startZ >> 4;
    System.out
        .printf("Geting blocks at (%d,%d,%d) chunks (%d,%d) radius (%d)", startX, startY, startZ,
                startChunkX, startChunkZ, radius);
    // System.out.printf("Getting list of blocks around coordinates ,
    // OVERLAY_BLOCKS)
    IChunkProvider provider = world.getChunkProvider();
    // Test code please ignore
    // for (int x=0; x<16; x++) {
    // for (int z=0;z<16;z++) {
    // Chunk chunk = provider.provideChunk(startChunkX,startChunkZ);
    // world.setBlock(startChunkX*16+x, 75, startChunkZ*16+z,
    // Blocks.bedrock);
    // }
    // }
    for (int chunkX = startChunkX - radius; chunkX <= startChunkX + radius; chunkX++) {
      for (int chunkZ = startChunkZ - radius; chunkZ <= startChunkZ + radius; chunkZ++) {
        if (provider.chunkExists(chunkX, chunkZ)) {
          Chunk chunk = provider.provideChunk(chunkX, chunkZ);
          for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
              // for (int y = startY; y > startY - distance && y
              // >= 0; y--) {
              for (int y = 254; y > 1; y--) {
                // turns local chunk coords => world coords
                int wx = chunkX * 16 + x;
                int wz = chunkZ * 16 + z;
                Block block = chunk.getBlock(wx & 15, y, wz & 15);
                if (block != null) {
                  if (!world.isAirBlock(wx, y, wz)) {
                    if (world.isAirBlock(wx, y + 1, wz)) {
                      if (world.doesBlockHaveSolidTopSurface(world, wx, y, wz)) {
                        chunkContents.add(new int[]{wx, y, wz});
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    System.out
        .printf("generation took %dms to collect %dblocks.\n", System.currentTimeMillis() - tStart,
                chunkContents.size());
    return chunkContents;
  }
}
