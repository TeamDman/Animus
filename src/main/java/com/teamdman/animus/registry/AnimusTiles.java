package com.teamdman.animus.registry;

import com.teamdman.animus.tiles.TileAntimatter;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AnimusTiles {
	public static void init() {
		GameRegistry.registerTileEntity(TileAntimatter.class,"tileantimatter");
	}

}
