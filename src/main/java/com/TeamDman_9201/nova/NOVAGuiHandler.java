package com.TeamDman_9201.nova;

import com.TeamDman_9201.nova.Containers.ContainerBrickFurnace;
import com.TeamDman_9201.nova.Containers.ContainerLightManipulator;
import com.TeamDman_9201.nova.Gui.GuiBrickFurnace;
import com.TeamDman_9201.nova.Gui.GuiLightManipulator;
import com.TeamDman_9201.nova.Tiles.TileEntityBrickFurnace;
import com.TeamDman_9201.nova.Tiles.TileEntityLightManipulator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class NOVAGuiHandler implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		switch(ID) {
			case NOVA.guiBrickFurnace:
				return new ContainerBrickFurnace(player.inventory, (TileEntityBrickFurnace)world.getTileEntity(x, y, z));//Object();//GuiPortalgestures();
			case NOVA.guiLightManipulator:
				return new ContainerLightManipulator(player.inventory, (TileEntityLightManipulator)world.getTileEntity(x, y, z));
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		// TODO Auto-generated method stub
		System.out.println("GUI Called with "+ID);
		switch(ID) {
			case NOVA.guiBrickFurnace:
				return new GuiBrickFurnace(player.inventory,(TileEntityBrickFurnace)world.getTileEntity(x, y, z));
			case NOVA.guiLightManipulator:
				return new GuiLightManipulator(player.inventory,(TileEntityLightManipulator)world.getTileEntity(x,y,z));
		}
		return null;
	}

}
