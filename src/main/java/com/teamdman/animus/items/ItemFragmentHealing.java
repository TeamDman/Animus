package com.teamdman.animus.items;

import WayofTime.bloodmagic.client.IVariantProvider;
import amerifrance.guideapi.api.util.TextHelper;
import com.teamdman.animus.Constants;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemFragmentHealing extends Item implements IVariantProvider {
	public ItemFragmentHealing() {
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.HEALING_FLAVOUR));
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.HEALING_INFO));
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
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "type=normal");
	}
}
