package com.teamdman.animus.items;


import WayofTime.bloodmagic.api.BlockStack;
import WayofTime.bloodmagic.api.altar.AltarComponent;
import WayofTime.bloodmagic.api.altar.EnumAltarTier;
import WayofTime.bloodmagic.api.altar.IBloodAltar;
import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.tile.TileAltar;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
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


/**
 * Created by TeamDman on 2015-08-30.
 */
public class ItemAltarDiviner extends Item implements IVariantProvider {

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.getTileEntity(pos)==null || !(world.getTileEntity(pos) instanceof IBloodAltar))
			return EnumActionResult.PASS;
		TileAltar altar = (TileAltar) world.getTileEntity(pos);
		if (!player.isSneaking() || altar == null || altar.getTier().toInt() > 4)
			return EnumActionResult.PASS;

		for (AltarComponent altarComponent : EnumAltarTier.values()[altar.getTier().toInt()].getAltarComponents()) {
			BlockPos componentPos = pos.add(altarComponent.getOffset());
			BlockStack worldBlock = new BlockStack(world.getBlockState(componentPos).getBlock(), world.getBlockState(componentPos).getBlock().getMetaFromState(world.getBlockState(componentPos)));


			if (world.isAirBlock(componentPos))
				world.setBlockState(componentPos, AnimusBlocks.blockPhantomBuilder.getDefaultState());
		}
		return EnumActionResult.PASS;
	}


	@Override
	public List<Pair<Integer, String>> getVariants()
	{
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}
