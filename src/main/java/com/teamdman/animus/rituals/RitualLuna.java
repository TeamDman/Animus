package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.Constants;
import com.teamdman.animus.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.Consumer;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualLuna extends Ritual {
	public static final String EFFECT_RANGE = "effect";
	public static final String CHEST_RANGE  = "chest";

	public RitualLuna() {
		super("ritualLuna", 0, 1000, "ritual." + Constants.Mod.MODID + ".luna");

		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
		addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
		setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World          world          = masterRitualStone.getWorldObj();
		SoulNetwork    network        = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		int            currentEssence = network.getCurrentEssence();
		BlockPos       masterPos      = masterRitualStone.getBlockPos();
		AreaDescriptor chestRange     = getBlockRange(CHEST_RANGE);
		TileEntity     tileInventory  = world.getTileEntity(chestRange.getContainedPositions(masterPos).get(0));


		if (!world.isRemote && tileInventory != null && tileInventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,EnumFacing.UP)) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}

			AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
			for (BlockPos pos : effectRange.getContainedPositions(masterPos)) {
				IBlockState state = world.getBlockState(pos);
				if (state.getBlock().getLightValue(state, world, pos) != 0) {
					IItemHandler handler = tileInventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,EnumFacing.UP);
					ItemStack stack = new ItemStack(state.getBlock().getItemDropped(state, world.rand, 0));
					if (ItemHandlerHelper.insertItem(handler, stack, true) == ItemStack.EMPTY) {
						ItemHandlerHelper.insertItem(handler, stack, false);
						world.setBlockToAir(pos);
						network.syphon(getRefreshCost());
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
	public void gatherComponents(Consumer<RitualComponent> components) {
		for (int layer = 0; layer < 3; layer++) {
			components.accept(new RitualComponent(new BlockPos(2,layer,2), EnumRuneType.DUSK));
			components.accept(new RitualComponent(new BlockPos(-2,layer,2), EnumRuneType.DUSK));
			components.accept(new RitualComponent(new BlockPos(2,layer,-2), EnumRuneType.DUSK));
			components.accept(new RitualComponent(new BlockPos(-2,layer,-2), EnumRuneType.DUSK));
		}
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualLuna();
	}

}