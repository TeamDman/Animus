package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.RegistrarBloodMagicBlocks;
import WayofTime.bloodmagic.core.RegistrarBloodMagicItems;
import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;


/**
 * Created by TeamDman on 2015-05-28.
 */
@RitualRegister(Constants.Rituals.SOL)
public class RitualSol extends Ritual {
	public static final String CHEST_RANGE  = "chest";
	public static final String EFFECT_RANGE = "effect";


	public RitualSol() {
		super(Constants.Rituals.SOL, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SOL);

		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
		addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
		setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
	}


	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World           world         = masterRitualStone.getWorldObj();
		SoulNetwork     network       = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		BlockPos        masterPos     = masterRitualStone.getBlockPos();
		AreaDescriptor  chestRange    = getBlockRange(CHEST_RANGE);
		TileEntityChest tileInventory = (TileEntityChest) world.getTileEntity(chestRange.getContainedPositions(masterPos).get(0));
		if (tileInventory == null)
			return;
		IItemHandler handler = tileInventory.getSingleChestHandler();

		if (!masterRitualStone.getWorldObj().isRemote) {
			Optional<Integer> slot = Stream.iterate(0, n -> ++n)
					.limit(handler.getSlots() - 1)
					.filter((e) -> !handler.getStackInSlot(e).isEmpty())
					.filter((e) -> this.isOkayToUse(handler.getStackInSlot(e)))
					.findAny();
			if (!slot.isPresent())
				return;
			Optional<BlockPos> toPlace = getBlockRange(EFFECT_RANGE).getContainedPositions(masterRitualStone.getBlockPos()).stream()
					.filter(world::isAirBlock)
					.filter((e) -> world.getLightFromNeighbors(e) < 8)
					.filter((e) -> world.isSideSolid(e.down(1), EnumFacing.UP))
					.findFirst();

			if (!toPlace.isPresent())
				return;
			IBlockState state = getStateToUse(handler.getStackInSlot(slot.get()));
			world.setBlockState(toPlace.get(), state);
			if (state.getBlock() != RegistrarBloodMagicBlocks.BLOOD_LIGHT) {
				handler.extractItem(slot.get(), 1, false);
			}
			network.syphon(new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_SOL), getRefreshCost()));
		}
	}

	private boolean isOkayToUse(ItemStack in) {
		return !in.isEmpty() && (in.getItem() == RegistrarBloodMagicItems.SIGIL_BLOOD_LIGHT || Block.getBlockFromItem(in.getItem()) != Blocks.AIR);
	}

	private IBlockState getStateToUse(ItemStack in) {
		if (in.getItem() == RegistrarBloodMagicItems.SIGIL_BLOOD_LIGHT) {
			return RegistrarBloodMagicBlocks.BLOOD_LIGHT.getDefaultState();
		} else {
			return Block.getBlockFromItem(in.getItem()).getStateFromMeta(in.getItemDamage());
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
			components.accept(new RitualComponent(new BlockPos(2, layer, 2), EnumRuneType.AIR));
			components.accept(new RitualComponent(new BlockPos(-2, layer, 2), EnumRuneType.AIR));
			components.accept(new RitualComponent(new BlockPos(2, layer, -2), EnumRuneType.AIR));
			components.accept(new RitualComponent(new BlockPos(-2, layer, -2), EnumRuneType.AIR));
		}
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualSol();
	}

}