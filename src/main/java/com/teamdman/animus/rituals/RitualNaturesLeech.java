package com.teamdman.animus.rituals;

import java.util.ArrayList;
import java.util.Random;

import com.teamdman.animus.Animus;
import com.teamdman.animus.client.resources.EffectHandler;
import com.teamdman.animus.client.resources.fx.EntityFXBurst;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.tile.TileAltar;

public class RitualNaturesLeech extends Ritual {
	public int reagentDrain = 2;

	public static final String EFFECT_RANGE = "effect";

	public RitualNaturesLeech() {
		super("ritualNaturesLeech", 0, 3000, "ritual." + Animus.MODID + ".naturesleech");

		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 20, 48, 48);
	}

	@Override
	public int getRefreshCost() {
		return 100;
	}

	@Override
	public ArrayList<RitualComponent> getComponents() {
		ArrayList<RitualComponent> ritualBlocks = new ArrayList<RitualComponent>();
		this.addRune(ritualBlocks, -3, 0, 0, EnumRuneType.AIR);
		this.addRune(ritualBlocks, -2, 0, -2, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, -2, 0, 2, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, -2, 1, -2, EnumRuneType.WATER);
		this.addRune(ritualBlocks, -2, 1, 0, EnumRuneType.AIR);
		this.addRune(ritualBlocks, -2, 1, 2, EnumRuneType.WATER);
		this.addRune(ritualBlocks, -1, 0, -1, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, -1, 0, 1, EnumRuneType.WATER);
		this.addRune(ritualBlocks, 0, 0, -3, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, 0, 0, 3, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, 0, 1, -2, EnumRuneType.AIR);
		this.addRune(ritualBlocks, 0, 1, 2, EnumRuneType.AIR);
		this.addRune(ritualBlocks, 1, 0, -1, EnumRuneType.WATER);
		this.addRune(ritualBlocks, 1, 0, 1, EnumRuneType.WATER);
		this.addRune(ritualBlocks, 2, 0, -2, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, 2, 0, 2, EnumRuneType.EARTH);
		this.addRune(ritualBlocks, 2, 1, -2, EnumRuneType.WATER);
		this.addRune(ritualBlocks, 2, 1, 0, EnumRuneType.AIR);
		this.addRune(ritualBlocks, 2, 1, 2, EnumRuneType.WATER);
		this.addRune(ritualBlocks, 3, 0, 0, EnumRuneType.EARTH);
		return ritualBlocks;
	}

	public void performRitual(IMasterRitualStone ritualStone) {
		Random random = new Random();
		World world = ritualStone.getWorldObj();

		int x = ritualStone.getBlockPos().getX();
		int y = ritualStone.getBlockPos().getY();
		int z = ritualStone.getBlockPos().getZ();
		int radius = 2;
		SoulNetwork network = NetworkHelper.getSoulNetwork(ritualStone.getOwner());
		int currentEssence = network.getCurrentEssence();
		TileAltar tileAltar = new TileAltar();

		if (!ritualStone.getWorldObj().isRemote) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}

			int eaten = 0;
			int max = 100;
			network.syphon(this.getRefreshCost());
			BlockPos at = null;

			for (int eat = 0; eat < 5; eat++) {
				if (eat > max)
					break;// little sanity checking
				
				int[] pos = getNextBlock(world, x, z, radius, ritualStone);

				if (pos != null) {
					if (random.nextInt(100) < 20) {
						at = new BlockPos(pos[0], pos[1], pos[2]);

						EffectHandler.getInstance()
								.registerFX(new EntityFXBurst(1, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + .5, 1F));

						world.playSound(null, at, AnimusSoundEventHandler.naturesleech, SoundCategory.BLOCKS, .4F, 1F);
						world.setBlockToAir(at);
						eaten++;

						boolean testFlag = false;
						// Lets find an altar!
						for (int i = -10; i <= 10; i++) {
							for (int j = -10; j <= 10; j++) {
								for (int k = -10; k <= 10; k++) {
									if (world.getTileEntity(new BlockPos(x + i, y + k, z + j)) instanceof TileAltar) {
										tileAltar = (TileAltar) world.getTileEntity(new BlockPos(x + i, y + k, z + j));
										testFlag = true;

									}
								}
							}
							if (!testFlag) {

								return; // No altar in range, abandon ship!
							} else {
								tileAltar.sacrificialDaggerCall(eaten * 8, true);
							}

						}
					} else // This block wasn't eaten
					{
						eat--;
					}

				}
			}
		}

	}

	public double smallGauss(double d) {
		Random myRand = new Random();
		return (myRand.nextFloat() - 0.5D) * d;
	}

	public int[] getNextBlock(World world, int ritualX, int ritualZ, int radius, IMasterRitualStone ritualStone) {
		int startChunkX = ritualX >> 4;
		int startChunkZ = ritualZ >> 4;
		double stoneY = 64;

		if (ritualStone != null)
			stoneY = ritualStone.getBlockPos().getY() - 10;// works up to 10 levels below the MRS

		IChunkProvider provider = world.getChunkProvider();
		for (int chunkX = startChunkX - radius; chunkX <= startChunkX + radius; chunkX++) {
			for (int chunkZ = startChunkZ - radius; chunkZ <= startChunkZ + radius; chunkZ++) {
				provider.getLoadedChunk(chunkX, chunkZ);
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						for (int y = (int) stoneY; y < (int) stoneY + 32; y++) { // works up  to 32 above the MRS
							int wx = chunkX * 16 + x;
							int wz = chunkZ * 16 + z;
							Block thisBlock = world.getBlockState(new BlockPos(wx, y, wz)).getBlock();
							String blockName = thisBlock.getUnlocalizedName().toLowerCase();
							if (thisBlock instanceof BlockCrops || thisBlock instanceof BlockLog
									|| thisBlock instanceof BlockLeaves || thisBlock instanceof BlockFlower
									|| thisBlock instanceof BlockTallGrass || thisBlock instanceof BlockDoublePlant
									|| blockName.contains("extrabiomesxl.flower"))
								if (!blockName.contains("specialflower") && !blockName.contains("shinyflower"))
									return new int[] { wx, y, wz };
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualNaturesLeech();
	}

}