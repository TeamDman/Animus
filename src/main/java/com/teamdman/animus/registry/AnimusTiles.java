package com.teamdman.animus.registry;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.tiles.TileAntimatter;
import com.teamdman.animus.tiles.TileBloodCore;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class AnimusTiles {
	public static void init() {
		GameRegistry.registerTileEntity(TileAntimatter.class, new ResourceLocation(Constants.Mod.NAME, "tileantimatter"));
		GameRegistry.registerTileEntity(TileBloodCore.class, new ResourceLocation(Constants.Mod.NAME, "tilebloodcore"));
	}

}
