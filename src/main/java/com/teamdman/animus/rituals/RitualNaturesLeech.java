package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.Animus;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.teamdman.animus.Constants;
import java.util.Random;
import java.util.function.Consumer;

public class RitualNaturesLeech extends Ritual {
	public static final String   EFFECT_RANGE   = "effect";
	public static final String   ALTAR_RANGE    = "altar";
	public              double   will           = 100;
	public              BlockPos altarOffsetPos = new BlockPos(0, 0, 0);

	public RitualNaturesLeech() {
		super("ritualNaturesLeech", 0, 3000, "ritual." + Constants.Mod.MODID + ".naturesleech");

		addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), 24));
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 20, 20, 20);
		setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);

	}

	@Override
	public int getRefreshCost() {
		return 10;
	}

	@Override
	public void gatherComponents(Consumer<RitualComponent> components) {
		components.accept(new RitualComponent(new BlockPos(-2, 1, -2), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(-2, 1, 0), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(-2, 1, 2), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(-1, 0, -1), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(-1, 0, 1), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(0, 1, -2), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(0, 1, 2), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(1, 0, -1), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(1, 0, 1), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(2, 1, -2), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(2, 1, 0), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(2, 1, 2), EnumRuneType.WATER));
	}

	public void performRitual(IMasterRitualStone ritualStone) {
		Random            random   = new Random();
		World             world    = ritualStone.getWorldObj();
		BlockPos          pos      = ritualStone.getBlockPos();
		EnumDemonWillType type     = EnumDemonWillType.CORROSIVE;
		BlockPos          altarPos = pos.add(altarOffsetPos);
		TileEntity        tile     = world.getTileEntity(altarPos);
		will = WorldDemonWillHandler.getCurrentWill(world, pos, type);

		SoulNetwork network        = NetworkHelper.getSoulNetwork(ritualStone.getOwner());
		int         currentEssence = network.getCurrentEssence();
		TileAltar   tileAltar      = new TileAltar();

		if (!ritualStone.getWorldObj().isRemote) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}

			int eaten = 0;
			network.syphon(this.getRefreshCost());

			AreaDescriptor altarRange = getBlockRange(ALTAR_RANGE);
			boolean        testFlag   = false;

			if (!altarRange.isWithinArea(altarOffsetPos) || !(tile instanceof TileAltar)) {
				for (BlockPos newPos : altarRange.getContainedPositions(pos)) {
					TileEntity nextTile = world.getTileEntity(newPos);
					if (nextTile instanceof TileAltar) {
						tile = nextTile;
						altarOffsetPos = newPos.subtract(pos);

						altarRange.resetCache();
						break;
					}
				}
			}

			if (tile instanceof TileAltar) {
				tileAltar = (TileAltar) tile;
				testFlag = true;
			}
			if (!testFlag) {
				return;
			}

			AreaDescriptor eatRange = getBlockRange(EFFECT_RANGE);

			eatRange.resetIterator();
			int randFood = 1 + random.nextInt(3);

			while (eatRange.hasNext() && eaten <= randFood) {

				BlockPos nextPos   = eatRange.next().add(pos);
				Block    thisBlock = world.getBlockState(nextPos).getBlock();
				if (thisBlock == Blocks.AIR)
					continue;

				boolean edible = false;

				if (random.nextInt(100) < 20) {
					String blockName = thisBlock.getUnlocalizedName().toLowerCase();

					if (thisBlock instanceof BlockCrops || thisBlock instanceof BlockLog
							|| thisBlock instanceof BlockLeaves || thisBlock instanceof BlockFlower
							|| thisBlock instanceof BlockTallGrass || thisBlock instanceof BlockDoublePlant
							|| blockName.contains("extrabiomesxl.flower"))
						edible = true;

					if (blockName.contains("specialflower") || blockName.contains("shinyflower"))
						edible = false;

					if (!edible)
						continue;

					//					EffectHandler.getInstance().registerFX(
					//							new EntityFXBurst(1, nextPos.getX() + 0.5, nextPos.getY() + 0.5, nextPos.getZ() + .5, 1F));

					if (world.isRemote) {
						world.spawnParticle(EnumParticleTypes.SPELL, nextPos.getX() + 0.5, nextPos.getY() + 0.5, nextPos.getZ() + .5,
								(random.nextDouble() - 0.5D) * 2.0D, -random.nextDouble(), (random.nextDouble() - 0.5D) * 2.0D, new int[0]);
					}

					world.playSound(null, nextPos, AnimusSoundEventHandler.naturesleech, SoundCategory.BLOCKS, .4F, 1F);
					world.setBlockToAir(nextPos);
					eaten++;

				}

			}

			tileAltar.sacrificialDaggerCall(eaten * 50, true);
			int drainAmount = 1 + (int) (Math.random() * ((5 - 1) + 1));
			if (will > 5 && random.nextInt(100) < 30) {
				WorldDemonWillHandler.drainWill(world, pos, type, drainAmount, true);
			}
		}

	}

	public double smallGauss(double d) {
		Random myRand = new Random();
		return (myRand.nextFloat() - 0.5D) * d;
	}

	@Override
	public int getRefreshTime() {
		int rt = (int) Math.min(80, (100 * (100 / (Math.max(1, will) * 6))));
		return rt;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualNaturesLeech();
	}

}