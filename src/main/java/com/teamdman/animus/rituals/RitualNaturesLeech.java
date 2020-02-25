package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.common.util.AnimusUtil;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

@RitualRegister(Constants.Rituals.LEECH)
public class RitualNaturesLeech extends Ritual {
	public static final String   ALTAR_RANGE    = "altar";
	public static final String   EFFECT_RANGE   = "effect";
	public final        int      maxWill        = 100;
	public              BlockPos altarOffsetPos = new BlockPos(0, 0, 0);
	public              double   will           = 100;

	public RitualNaturesLeech() {
		super(Constants.Rituals.LEECH, 0, 3000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.LEECH);

		addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), 24));
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 20, 20, 20);
		setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
	}

	public static boolean isBlacklisted(ResourceLocation resourceLocation) {
		return resourceLocation != null && isBlacklisted(resourceLocation.toString());
	}

	public static boolean isBlacklisted(String registryName) {
		for (String entry : AnimusConfig.sigils.leechBlacklist)
			if (Objects.equals(entry, registryName))
				return true;
		return false;
	}

	public void performRitual(IMasterRitualStone ritualStone) {
		World             world  = ritualStone.getWorldObj();
		Random            random = world.rand;
		BlockPos          pos    = ritualStone.getBlockPos();
		EnumDemonWillType type   = EnumDemonWillType.CORROSIVE;
		will = WorldDemonWillHandler.getCurrentWill(world, pos, type);

		SoulNetwork network        = NetworkHelper.getSoulNetwork(ritualStone.getOwner());
		int         currentEssence = network.getCurrentEssence();

		if (!ritualStone.getWorldObj().isRemote) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}

			network.syphon(new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_LEECH), this.getRefreshCost()));

			TileAltar tileAltar = AnimusUtil.getNearbyAltar(world, ritualStone.getBlockRange(ALTAR_RANGE), pos, altarOffsetPos);
			if (tileAltar == null)
				return;
			altarOffsetPos = tileAltar.getPos();

			AreaDescriptor eatRange = ritualStone.getBlockRange(EFFECT_RANGE);
			eatRange.resetIterator();
			int randFood = 1 + random.nextInt(3);
			int eaten    = 0;
			while (eatRange.hasNext() && eaten <= randFood) {

				BlockPos eatPos   = eatRange.next().add(pos);
				Block    eatBlock = world.getBlockState(eatPos).getBlock();
				if (eatBlock == Blocks.AIR)
					continue;

				if (random.nextInt(100) < 20 && isConsumable(eatBlock)) {
					//					EffectHandler.getInstance().registerFX(
					//							new EntityFXBurst(1, nextPos.getX() + 0.5, nextPos.getY() + 0.5, nextPos.getZ() + .5, 1F));
					if (world.isRemote) {
						world.spawnParticle(EnumParticleTypes.SPELL, eatPos.getX() + 0.5, eatPos.getY() + 0.5, eatPos.getZ() + .5,
								(random.nextDouble() - 0.5D) * 2.0D, -random.nextDouble(), (random.nextDouble() - 0.5D) * 2.0D);
					}

					//TODO: fix particles and sounds
					world.playSound(null, eatPos, AnimusSoundEventHandler.naturesleech, SoundCategory.BLOCKS, .4F, 1F);
					world.setBlockToAir(eatPos);
					eaten++;
				}
			}

			tileAltar.sacrificialDaggerCall(eaten * 50, true);
			int    drainAmount = 1 + (int) (Math.random() * ((5 - 1) + 1));
			double filled      = WorldDemonWillHandler.fillWillToMaximum(world, pos, type, drainAmount, maxWill, false);
			if (filled > 0)
				WorldDemonWillHandler.fillWillToMaximum(world, pos, type, filled, maxWill, true);


		}

	}

	public static boolean isConsumable(Block block) {
		if (block == null)
			return false;
		if (block == Blocks.AIR)
			return false;
		String blockName = block.getTranslationKey().toLowerCase();
		if (blockName.contains("specialflower") || blockName.contains("shinyflower"))
			return false;
		if (!(block instanceof BlockLog
				|| block instanceof IPlantable
				|| block instanceof IShearable
				|| block instanceof IGrowable
				|| blockName.contains("extrabiomesxl.flower")))
			return false;
		return !isBlacklisted(block.getRegistryName());
	}

	@Override
	public int getRefreshCost() {
		return 10;
	}

	@Override
	public int getRefreshTime() {
		return (int) Math.min(80, (100 * (100 / (Math.max(1, will) * 6))));
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

	@Override
	public Ritual getNewCopy() {
		return new RitualNaturesLeech();
	}

}
