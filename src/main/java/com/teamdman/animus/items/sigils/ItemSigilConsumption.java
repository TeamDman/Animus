package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.item.sigil.ItemSigilBase;
import amerifrance.guideapi.api.util.TextHelper;
import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.BlockAntimatter;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.tiles.TileAntimatter;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilConsumption extends ItemSigilBase implements IVariantProvider {
	public ItemSigilConsumption() {
		super(Constants.Sigils.CONSUMPTION, 200);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.getTileEntity(blockPos) != null || world.getBlockState(blockPos).getBlock().getBlockHardness(null, null, null) == -1.0F)
			return EnumActionResult.SUCCESS;
		Block seeking = world.getBlockState(blockPos).getBlock();
		world.setBlockState(blockPos, AnimusBlocks.BLOCKANTIMATTER.getDefaultState().withProperty(BlockAntimatter.DECAYING, false));

		((TileAntimatter) world.getTileEntity(blockPos)).seeking = seeking;
		((TileAntimatter) world.getTileEntity(blockPos)).player = player;

		world.scheduleBlockUpdate(blockPos, AnimusBlocks.BLOCKANTIMATTER, 5, 0);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(TextHelper.localize(Constants.Localizations.Tooltips.SIGIL_CONSUMPTION_FLAVOUR));
		super.addInformation(stack, world, tooltip, flag);
	}

}