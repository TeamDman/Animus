package com.teamdman.animus.registry;

import WayofTime.bloodmagic.BloodMagic;
import WayofTime.bloodmagic.ConfigHandler;
import com.teamdman.animus.Animus;
import com.teamdman.animus.blocks.BlockPhantomBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by TeamDman on 9/25/2016.
 */
public class AnimusBlocks {
	public static Block blockPhantomBuilder;

	public static void init() {
		blockPhantomBuilder = setupBlock(new BlockPhantomBuilder(), "blockPhantomBuilder");
	}

	private static Block setupBlock(Block block, String name) {
		if (ConfigHandler.blockBlacklist.contains(name))
			return block;
		if (block.getRegistryName() == null)
			block.setRegistryName(name);

		block.setUnlocalizedName(name);
		GameRegistry.register(block);
		GameRegistry.register(new ItemBlock(block).setRegistryName(name));
		block.setCreativeTab(Animus.tabMain);
		Animus.proxy.tryHandleBlockModel(block, name);
		return block;
	}
}
