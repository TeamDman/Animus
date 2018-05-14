package com.teamdman.animus.blocks;

import WayofTime.bloodmagic.client.IVariantProvider;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.tiles.TileAntimatter;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Random;

public class BlockAntimatter extends Block implements IVariantProvider {
	public static final PropertyBool DECAYING = PropertyBool.create("decaying");

	public BlockAntimatter() {
		super(Material.SPONGE);
	}

	public static EnumActionResult setBlockToAntimatter(World world, BlockPos blockPos, EntityPlayer player) {
		if (world.getTileEntity(blockPos) != null || world.getBlockState(blockPos).getBlock().getBlockHardness(null, null, null) == -1.0F)
			return EnumActionResult.PASS;
		Block seeking = world.getBlockState(blockPos).getBlock();
		world.setBlockState(blockPos, AnimusBlocks.BLOCKANTIMATTER.getDefaultState().withProperty(BlockAntimatter.DECAYING, false));

		((TileAntimatter) world.getTileEntity(blockPos)).seeking = seeking;
		((TileAntimatter) world.getTileEntity(blockPos)).player = player;

		world.scheduleBlockUpdate(blockPos, AnimusBlocks.BLOCKANTIMATTER, 5, 0);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		TileAntimatter tile     = (TileAntimatter) worldIn.getTileEntity(pos);
		boolean        decaying = state.getValue(DECAYING);
		//noinspection ConstantConditions
		if (tile.range <= 0) {
			return;
		}
		for (BlockPos newpos : getNeighbours(pos)) {
			if (decaying) {
				if (worldIn.getBlockState(newpos).getBlock() == AnimusBlocks.BLOCKANTIMATTER) {
					worldIn.setBlockState(newpos, getDefaultState().withProperty(DECAYING, true));
					worldIn.scheduleBlockUpdate(newpos, AnimusBlocks.BLOCKANTIMATTER, 0, 1);
					worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.01F, 0.75F);
				}
			} else if (!worldIn.isAirBlock(newpos) && worldIn.getBlockState(newpos).getBlock() == tile.seeking) {
				worldIn.setBlockState(newpos, AnimusBlocks.BLOCKANTIMATTER.getDefaultState().withProperty(DECAYING, false));
				//noinspection ConstantConditions
				((TileAntimatter) worldIn.getTileEntity(newpos)).seeking = tile.seeking;
				//noinspection ConstantConditions
				((TileAntimatter) worldIn.getTileEntity(newpos)).range = tile.range - 1;
				//noinspection ConstantConditions
				((TileAntimatter) worldIn.getTileEntity(newpos)).player = tile.player;
				worldIn.scheduleBlockUpdate(newpos, AnimusBlocks.BLOCKANTIMATTER, worldIn.rand.nextInt(25), 1);
				if (tile.player != null)
					NetworkHelper.getSoulNetwork(tile.player).syphonAndDamage(tile.player, AnimusConfig.sigils.antimatterConsumption);
				worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 0.01F, 0.75F);
			}
		}
		if (decaying)
			worldIn.setBlockToAir(pos);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(DECAYING) ? 1 : 0;
	}

	@Override
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
		for (BlockPos newpos : getNeighbours(pos)) {
			if (worldIn.getBlockState(newpos).getBlock() == AnimusBlocks.BLOCKANTIMATTER) {
				worldIn.setBlockState(newpos, getDefaultState().withProperty(DECAYING, true));
				worldIn.scheduleBlockUpdate(newpos, AnimusBlocks.BLOCKANTIMATTER, 5, 1);
			}
		}
		worldIn.setBlockToAir(pos);
	}

	private ArrayList<BlockPos> getNeighbours(BlockPos pos) {
		ArrayList<BlockPos> neighbours = new ArrayList<>();
		for (int x = -1; x <= 1; x++)
			for (int y = -1; y <= 1; y++)
				for (int z = -1; z <= 1; z++)
					neighbours.add(pos.add(x, y, z));
		return neighbours;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, DECAYING);
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileAntimatter();
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
	}

	@Override
	public void gatherVariants(@Nonnull Int2ObjectMap<String> variants) {
		variants.put(0, "normal");
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
}
