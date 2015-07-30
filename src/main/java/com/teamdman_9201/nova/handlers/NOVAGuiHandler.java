package com.teamdman_9201.nova.handlers;

import com.teamdman_9201.nova.NOVA;
import com.teamdman_9201.nova.containers.ContainerDirtChest;
import com.teamdman_9201.nova.gui.GuiDirtChest;
import com.teamdman_9201.nova.tiles.TileDirtChest;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

public class NOVAGuiHandler implements IGuiHandler {

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int
            z) {
        System.out.println("GUI Called with " + ID);
        switch (ID) {
            case NOVA.guiDirtChest:
                return new GuiDirtChest(player.inventory, (TileDirtChest) world.getTileEntity(x,
                        y, z));
        }
        return null;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int
            z) {
        switch (ID) {
            case NOVA.guiDirtChest:
                return new ContainerDirtChest(player.inventory, (TileDirtChest) world
                        .getTileEntity(x, y, z));

        }
        return null;
    }

}
