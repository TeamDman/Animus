package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ItemFragmentHealing extends Item implements IVariantProvider {
	public ItemFragmentHealing() {
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add("More the merrier!");
		tooltip.add("This item cannot be moved.");
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
		return false;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public boolean canHarvestBlock(IBlockState blockIn) {
		return false;
	}

	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}
