package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.util.Utils;
import com.teamdman.animus.Animus;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualLuna extends Ritual {
	public static final String EFFECT_RANGE = "effect";
	public static final String CHEST_RANGE = "chest";

	public RitualLuna() {
		super("ritualLuna", 1, 1000, "ritual." + Animus.MODID + ".lunaRitual");

		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
		addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
		setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World world = masterRitualStone.getWorldObj();
		SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		int currentEssence = network.getCurrentEssence();
		BlockPos masterPos = masterRitualStone.getBlockPos();
		AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
		TileEntity tileInventory = world.getTileEntity(chestRange.getContainedPositions(masterPos).get(0));


		if (!masterRitualStone.getWorldObj().isRemote && tileInventory != null && tileInventory instanceof IInventory) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}

			AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
			for (BlockPos pos : effectRange.getContainedPositions(masterRitualStone.getBlockPos())) {
				IBlockState state = world.getBlockState(pos);
				if (state.getBlock().getLightValue(state, world, pos) != 0) {
					ItemStack stack = new ItemStack(state.getBlock().getItemDropped(state, world.rand, 0));
					if (Utils.canInsertStackFullyIntoInventory(stack, (IInventory) tileInventory, EnumFacing.UP)) {
						Utils.insertStackIntoInventory(stack, (IInventory) tileInventory, EnumFacing.UP);
						world.setBlockToAir(pos);
						return;
					}
				}
			}
		}
	}

	@Override
	public int getRefreshCost() {
		return 1;
	}

	@Override
	public int getRefreshTime() {
		return 5;
	}

	@Override
	public ArrayList<RitualComponent> getComponents() {
		ArrayList<RitualComponent> components = new ArrayList();
		this.addParallelRunes(components, 1, 0, EnumRuneType.DUSK);
		this.addCornerRunes(components, 1, 0, EnumRuneType.DUSK);
		this.addParallelRunes(components, 2, 1, EnumRuneType.DUSK);
		this.addCornerRunes(components, 2, 1, EnumRuneType.DUSK);

		return components;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualLuna();
	}

}