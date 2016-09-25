package com.teamdman.animus.proxy;

import WayofTime.bloodmagic.util.helper.InventoryRenderHelperV2;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Created by TeamDman on 9/18/2016.
 */
public class CommonProxy {

	public void preInit(FMLPreInitializationEvent event) {

	}

	public void init(FMLInitializationEvent event) {

	}

	public void postInit(FMLPostInitializationEvent event) {

	}

	public void tryHandleItemModel(Item item, String name) {
	}

	public void tryHandleBlockModel(Block block, String name) {
	}

	public InventoryRenderHelperV2 getRenderHelper() {
		return null;
	}


}
