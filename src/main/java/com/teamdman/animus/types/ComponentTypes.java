package com.teamdman.animus.types;

import WayofTime.bloodmagic.item.types.ISubItem;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Locale;

public enum ComponentTypes implements ISubItem {
	REAGENT_BUILDER,
	REAGENT_CHAINS,
	REAGENT_CONSUMPTION,
	REAGENT_LEECH,
	REAGENT_STORM,
	REAGENT_TRANSPOSITION;

	@Nonnull
	@Override
	public String getInternalName() {
		return name().toLowerCase(Locale.ROOT);
	}

	@Nonnull
	@Override
	public ItemStack getStack() {
		return getStack(1);
	}

	@Nonnull
	@Override
	public ItemStack getStack(int count) {
		return new ItemStack(AnimusItems.COMPONENT, count, ordinal());
	}
}
