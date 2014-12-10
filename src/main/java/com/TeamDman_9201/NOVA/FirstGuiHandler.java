package com.TeamDman_9201.NOVA;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class FirstGuiHandler implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		switch(ID) {
			case First.guiBrickFurnace:
				return new ContainerBrickFurnace(player.inventory, (TileEntityBrickFurnace)world.getTileEntity(x, y, z));//Object();//GuiPortalgestures();
			case First.guiLightManipulator:
				return new ContainerLightManipulator(player.inventory, (TileEntityLightManipulator)world.getTileEntity(x, y, z));
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		System.out.println("GUI Called with "+ID);
		switch(ID) {
			case First.guiBrickFurnace:
				return new GuiBrickFurnace(player.inventory,(TileEntityBrickFurnace)world.getTileEntity(x, y, z));
			case First.guiLightManipulator:
				return new GuiLightManipulator(player.inventory,(TileEntityLightManipulator)world.getTileEntity(x,y,z));
		}
		return null;
	}

}
