package com.teamdman.animus.items;

import javax.annotation.Nonnull;

import WayofTime.bloodmagic.client.IVariantProvider;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.Item;

public class ItemActivationCrystalFragile extends Item implements IVariantProvider  {

	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}
	
}
