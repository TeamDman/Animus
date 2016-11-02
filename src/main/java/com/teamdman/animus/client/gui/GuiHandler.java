package com.teamdman.animus.client.gui;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);

		switch (id) {
			//        case Constants.Gui.TELEPOSER_GUI:
			//            return new ContainerTeleposer(player.inventory, (TileTeleposer) world.getTileEntity(pos));
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		if (world instanceof WorldClient) {
			BlockPos pos = new BlockPos(x, y, z);

			switch (id) {
				//            case Constants.Gui.TELEPOSER_GUI:
				//                return new GuiTeleposer(player.inventory, (TileTeleposer) world.getTileEntity(pos));
			}

		}
		return null;
	}
}