package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import com.teamdman.animus.Animus;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.EnumFaceDirection;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

import static WayofTime.bloodmagic.ritual.RitualGreenGrove.HYDRATE_RANGE;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualSol extends Ritual {
	public static final String EFFECT_RANGE = "effect";
	public static final String CHEST_RANGE = "chest";

	public RitualSol() {
		super("ritualSol", 0, 1000, "ritual." + Animus.MODID + ".solritual");

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
			int slotToPlace = -1;
			for (int slot = 0; slot < ((IInventory) tileInventory).getSizeInventory(); slot++) {
				if (((IInventory) tileInventory).getStackInSlot(slot)!=null &&  Block.getBlockFromItem(((IInventory) tileInventory).getStackInSlot(slot).getItem()) != null) {
					slotToPlace = slot;
				}
			}
			if (slotToPlace==-1)
				return;

			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}

			AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
			for (BlockPos pos : effectRange.getContainedPositions(masterRitualStone.getBlockPos())) {
				if (world.isAirBlock(pos) && world.getLightFromNeighbors(pos) < 8 && world.isSideSolid(pos.down(1), EnumFacing.UP.UP)) {
					IBlockState toPlace = Block.getBlockFromItem(((IInventory) tileInventory).getStackInSlot(slotToPlace).getItem()).getStateFromMeta(((IInventory) tileInventory).getStackInSlot(slotToPlace).getItemDamage());
					((IInventory) tileInventory).decrStackSize(slotToPlace,1);
					world.setBlockState(pos, toPlace);
					network.syphon(getRefreshCost());

					return;
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
		this.addParallelRunes(components, 0, -1, EnumRuneType.AIR);
		this.addParallelRunes(components, 0, -2, EnumRuneType.AIR);
		this.addParallelRunes(components, 0, -3, EnumRuneType.AIR);
		this.addParallelRunes(components, 1, -3, EnumRuneType.AIR);

		return components;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualSol();
	}

}