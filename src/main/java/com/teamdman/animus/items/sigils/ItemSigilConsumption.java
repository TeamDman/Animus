package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.item.sigil.ItemSigilBase;
import com.teamdman.animus.blocks.BlockAntimatter;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.tiles.TileAntimatter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilConsumption extends ItemSigilBase implements IVariantProvider {
	public ItemSigilConsumption() {
		super("consumption", 200);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos blockPos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.getTileEntity(blockPos) != null || world.getBlockState(blockPos).getBlock().getBlockHardness(null, null, null) == -1.0F)
			return EnumActionResult.SUCCESS;
		Block seeking = world.getBlockState(blockPos).getBlock();
		world.setBlockState(blockPos, AnimusBlocks.blockAntimatter.getDefaultState().withProperty(BlockAntimatter.DECAYING, false));
		((TileAntimatter) world.getTileEntity(blockPos)).seeking = seeking;
		((TileAntimatter) world.getTileEntity(blockPos)).player = player;

		world.scheduleBlockUpdate(blockPos, AnimusBlocks.blockAntimatter, 5, 0);
		return EnumActionResult.SUCCESS;
	}
}