package com.TeamDman_9201.nova.Tiles;

import com.TeamDman_9201.nova.GenericInventory;
import com.TeamDman_9201.nova.Threads.ThreadLightManipulator;

import net.minecraft.inventory.ISidedInventory;

import java.util.ArrayList;

public class TileLightManipulator extends GenericInventory implements ISidedInventory {

  private ThreadLightManipulator thread = new ThreadLightManipulator(this);
  private int iteration = 0;
  private int speed = 25;
  private boolean isActive = false;
  private ArrayList<int[]> chunkContents;
  public int getSizeInventory() {
    return this.items.length;
  }

  public TileLightManipulator() {
    super(8,"Light Manipulator", new int[] {0,7}, new int[] {0,7}, new int[] {0,7});
  }

  public void commence() {
    if (!this.worldObj.isRemote && !this.isActive) {
      this.isActive = true;
      chunkContents = thread.getBlocksInChunk(this.worldObj, false, 1);
      System.out.println("[FIRST DEBUG] Should have executed");
    }
  }

  public void updateEntity() {
    if (!this.worldObj.isRemote) {
      if (chunkContents != null && chunkContents.size() > 0) {
        for (int i = 0; i < speed; i++) {
          if (chunkContents.size() > iteration) {
            int[] coords = chunkContents.get(iteration);
            int x = coords[0];
            int y = coords[1];
            int z = coords[2];
            boolean placed = thread.correctLight(this.worldObj, x, y, z);
            iteration++;
            if (placed) {
              break;
            }
          } else {
            chunkContents = null;
            iteration = 0;
            this.isActive = false;
            break;
          }
        }
      }
    }
  }
}
