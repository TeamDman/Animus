package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.soul.EnumDemonWillType;
import WayofTime.bloodmagic.api.util.helper.LogHelper;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import WayofTime.bloodmagic.tile.TileAltar;
import com.teamdman.animus.Animus;
import com.teamdman.animus.client.resources.EffectHandler;
import com.teamdman.animus.client.resources.fx.EntityFXBurst;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class RitualCulling extends Ritual {
	public static final String EFFECT_RANGE = "effect";
	public static final String ALTAR_RANGE = "altar";

	public RitualCulling() {
		super("ritualCulling", 0, 50000, "ritual." + Animus.MODID + ".culling");

		addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), 21));

		setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 15, 15);

	}

	DamageSource culled = new DamageSource("animus.absolute").setDamageAllowedInCreativeMode().setDamageBypassesArmor()
			.setDamageIsAbsolute();

	public int reagentDrain = 2;
	public boolean result = false;
	public static final int amount = 200;
	public BlockPos altarOffsetPos = new BlockPos(0, 0, 0);
	public LogHelper logger = new LogHelper("Animus Debug");
	public double willBuffer = 0;
	public double crystalBuffer = 0;
	public final int maxWill = 100;
	public HashMap<EnumDemonWillType, Double> willMap = new HashMap<EnumDemonWillType, Double>();
	public Random rand = new Random();

	@Override
	public boolean activateRitual(IMasterRitualStone ritualStone, EntityPlayer player, String owner) {
		double xCoord, yCoord, zCoord;

		xCoord = ritualStone.getBlockPos().getX();
		yCoord = ritualStone.getBlockPos().getY();
		zCoord = ritualStone.getBlockPos().getZ();

		if (player != null)
			player.world.addWeatherEffect(new EntityLightningBolt(player.world, xCoord, yCoord, zCoord, false));

		return true;
	}

	@Override
	public void performRitual(IMasterRitualStone ritualStone) {
		SoulNetwork network = NetworkHelper.getSoulNetwork(ritualStone.getOwner());
		if (network == null) {
			return;
		}

		int currentEssence = network.getCurrentEssence();
		World world = ritualStone.getWorldObj();
		World soundSource = ritualStone.getWorldObj();
		EnumDemonWillType type = EnumDemonWillType.DESTRUCTIVE;
		BlockPos pos = ritualStone.getBlockPos();
		double currentAmount = WorldDemonWillHandler.getCurrentWill(world, pos, type);

		TileAltar tileAltar = null;
		boolean testFlag = false;

		BlockPos altarPos = pos.add(altarOffsetPos);

		TileEntity tile = world.getTileEntity(altarPos);

		AreaDescriptor altarRange = getBlockRange(ALTAR_RANGE);

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

		AreaDescriptor damageRange = getBlockRange(EFFECT_RANGE);
		AxisAlignedBB range = damageRange.getAABB(pos);

		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, range);

		int entityCount = 0;

		if (currentEssence < this.getRefreshCost() * list.size()) {
			network.causeNausea();
		} else {
			for (EntityLivingBase livingEntity : list) {
				if (ConfigHandler.wellOfSufferingBlacklist.contains(livingEntity.getClass().getSimpleName()))
					continue;

				if (livingEntity instanceof EntityPlayer && livingEntity.getHealth() > 4)
					continue;

				Collection<PotionEffect> effect = livingEntity.getActivePotionEffects(); // Disallows cursed earth spawned mobs

				if (effect.isEmpty()) {
					float damage = 0;
					BlockPos at = null;
					soundSource = livingEntity.world;
					at = livingEntity.getPosition();
					boolean isNonBoss = livingEntity.isNonBoss();

					if (livingEntity.getName().contains("Gaia"))
						continue;

					livingEntity.setSilent(true); // The screams of the weak fall on deaf ears.

					damage = Integer.MAX_VALUE;

					if (!isNonBoss && currentAmount > 99
							&& (currentEssence >= 50000 + (this.getRefreshCost() * list.size()))) { // Special case for Bosses, they require maxed vengeful will and 50k LP per kill
						livingEntity.setEntityInvulnerable(false);
						if (livingEntity instanceof EntityWither) {
							EntityWither EW = (EntityWither) livingEntity;
							EW.setInvulTime(0);
						}
					}

					result = (livingEntity.attackEntityFrom(culled, damage));

					if (result != false) {
						entityCount++;
						tileAltar.sacrificialDaggerCall(RitualCulling.amount, true);

						if (!isNonBoss) {
							network.syphon(50000);
						} else {
							double modifier = .5;
							if (livingEntity instanceof EntityAnimal && !livingEntity.isCollided) {
								modifier = 2;
							}
							willBuffer += modifier * Math.min(15.00, livingEntity.getMaxHealth());
						}

						if (at != null) {

							EffectHandler.getInstance().registerFX(
									new EntityFXBurst(0, at.getX() + 0.5, at.getY() + .8, at.getZ() + 0.5, 1F));
							soundSource.playSound(null, at, AnimusSoundEventHandler.ghostly, SoundCategory.BLOCKS, 1F,
									1F);

						}

					}
				}

			}

			network.syphon(getRefreshCost() * entityCount);
			double drainAmount = Math.min(maxWill - currentAmount, Math.min(entityCount/2, 10));

			if (rand.nextInt(30) == 0) { // 3% chance per cycle to generate destructive will
				double filled = WorldDemonWillHandler.fillWillToMaximum(world, pos, type, drainAmount, maxWill, false);
				if (filled > 0)
					WorldDemonWillHandler.fillWillToMaximum(world, pos, type, filled, maxWill, true);
			}
		}

	}

	public double smallGauss(double d) {
		Random myRand = new Random();
		return (myRand.nextFloat() - 0.5D) * d;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualCulling();
	}

	@Override
	public int getRefreshTime() {
		return 25;
	}

	@Override
	public int getRefreshCost() {
		return 75;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		willBuffer = tag.getDouble("willBuffer");

	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setDouble("willBuffer", willBuffer);

	}

	@Override
	public ArrayList<RitualComponent> getComponents() {
		ArrayList<RitualComponent> ritualBlocks = new ArrayList<RitualComponent>();

		this.addRune(ritualBlocks, 1, 0, 1, EnumRuneType.FIRE);
		this.addRune(ritualBlocks, -1, 0, 1, EnumRuneType.FIRE);
		this.addRune(ritualBlocks, 1, 0, -1, EnumRuneType.FIRE);
		this.addRune(ritualBlocks, -1, 0, -1, EnumRuneType.FIRE);
		this.addRune(ritualBlocks, 2, -1, 2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 2, -1, -2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -2, -1, 2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -2, -1, -2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 0, -1, 2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 2, -1, 0, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 0, -1, -2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -2, -1, 0, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -3, -1, -3, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 3, -1, -3, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -3, -1, 3, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 3, -1, 3, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 2, -1, 4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 4, -1, 2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -2, -1, 4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 4, -1, -2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 2, -1, -4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -4, -1, 2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -2, -1, -4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -4, -1, -2, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 1, 0, 4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 4, 0, 1, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 1, 0, -4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -4, 0, 1, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -1, 0, 4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 4, 0, -1, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -1, 0, -4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -4, 0, -1, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 4, 1, 0, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 0, 1, 4, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, -4, 1, 0, EnumRuneType.DUSK);
		this.addRune(ritualBlocks, 0, 1, -4, EnumRuneType.DUSK);
		return ritualBlocks;

	}

}