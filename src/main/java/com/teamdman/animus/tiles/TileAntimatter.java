package com.teamdman.animus.tiles;

import com.teamdman.animus.AnimusConfig;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;

public class TileAntimatter extends TileEntity {
	public Block        seeking;
	public int          range;
	public EntityPlayer player;

	public TileAntimatter() {
		this.seeking = Blocks.AIR;
		this.range = AnimusConfig.sigils.antimatterRange;
	}

	public TileAntimatter setSeeking(Block seeking) {
		this.seeking = seeking;
		return this;
	}

	public TileAntimatter setPlayer(EntityPlayer player) {
		this.player = player;
		return this;
	}
}
