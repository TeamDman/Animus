package com.teamdman_9201.nova.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileCompressedTorch extends TileEntity {

    long torches = 0;

    public void checkTags(NBTTagCompound nbt) {
    }

    public long getTorches() {
        return torches;
    }

    public void setTorches(long val) {
        torches = val;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        torches = tagCompound.getLong("Torches");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setLong("Torches", torches);
    }
}
