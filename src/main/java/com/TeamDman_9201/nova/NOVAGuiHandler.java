package com.TeamDman.nova;

import com.TeamDman.nova.Containers.ContainerBrickFurnace;
import com.TeamDman.nova.Gui.GuiBrickFurnace;
import com.TeamDman.nova.Gui.GuiLightManipulator;
import com.TeamDman.nova.Tiles.TileBrickFurnace;
import com.TeamDman.nova.Tiles.TileCobblizer;
import com.TeamDman.nova.Containers.ContainerCobblizer;
import com.TeamDman.nova.Containers.ContainerDirtChest;
import com.TeamDman.nova.Containers.ContainerLightManipulator;
import com.TeamDman.nova.Gui.GuiCobblizer;
import com.TeamDman.nova.Gui.GuiDirtChest;
import com.TeamDman.nova.Tiles.TileDirtChest;
import com.TeamDman.nova.Tiles.TileLightManipulator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

public class NOVAGuiHandler implements IGuiHandler {

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    // TODO Auto-generated method stub
    System.out.println("GUI Called with " + ID);
    switch (ID) {
      case NOVA.guiBrickFurnace:
        return new GuiBrickFurnace(player.inventory,
                                   (TileBrickFurnace) world.getTileEntity(x, y, z));
      case NOVA.guiLightManipulator:
        return new GuiLightManipulator(player.inventory,
                                       (TileLightManipulator) world.getTileEntity(x, y, z));
      case NOVA.guiCobblizer:
        return new GuiCobblizer(player.inventory,
                                       (TileCobblizer) world.getTileEntity(x, y, z));
      case NOVA.guiDirtChest:
        return new GuiDirtChest(player.inventory, (TileDirtChest) world.getTileEntity(x, y, z));
    }
    return null;
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    // TODO Auto-generated method stub
    switch (ID) {
      case NOVA.guiBrickFurnace:
        return new ContainerBrickFurnace(player.inventory, (TileBrickFurnace) world
            .getTileEntity(x, y, z));//Object();//GuiPortalgestures();
      case NOVA.guiLightManipulator:
        return new ContainerLightManipulator(player.inventory,
                                             (TileLightManipulator) world.getTileEntity(x, y, z));
      case NOVA.guiCobblizer:
        return new ContainerCobblizer(player.inventory,
                                       (TileCobblizer) world.getTileEntity(x, y, z));
      case NOVA.guiDirtChest:
        return new ContainerDirtChest(player.inventory,
                                      (TileDirtChest) world.getTileEntity(x, y, z));

    }
    return null;
  }

}
