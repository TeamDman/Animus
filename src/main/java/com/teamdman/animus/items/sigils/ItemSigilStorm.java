package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.api.impl.ItemSigil;
import WayofTime.bloodmagic.client.IVariantProvider;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ItemSigilStorm extends ItemSigil implements IVariantProvider {
	public ItemSigilStorm() {
		super(AnimusConfig.transpositionConsumption);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack player, EntityPlayer worldIn, World pos, BlockPos hand, EnumHand facing, EnumFacing hitX, float hitY, float hitZ, float p_180614_9_) {
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ, p_180614_9_);
	}

	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}
