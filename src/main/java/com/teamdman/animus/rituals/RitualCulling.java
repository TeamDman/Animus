package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import amerifrance.guideapi.util.LogHelper;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.common.util.AnimusUtil;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.naming.CompositeName;
import java.util.*;
import java.util.function.Consumer;

@RitualRegister(Constants.Rituals.CULLING)
public class RitualCulling extends Ritual {
	public static final String                             ALTAR_RANGE    = "altar";
	public static final String                             EFFECT_RANGE   = "effect";
	public static final int                                amount         = 200;
	static final        DamageSource                       culled         = new DamageSource(Constants.Misc.DAMAGE_ABSOLUTE).setDamageAllowedInCreativeMode().setDamageBypassesArmor().setDamageIsAbsolute();
	public final        int                                maxWill        = 100;
	public final        Random                             rand           = new Random();
	public              BlockPos                           altarOffsetPos = new BlockPos(0, 0, 0);
	public              double                             crystalBuffer  = 0;
	public              LogHelper                          logger         = new LogHelper();
	public              int                                reagentDrain   = 2;
	public              boolean                            result         = false;
	public              double                             willBuffer     = 0;
	public              HashMap<EnumDemonWillType, Double> willMap        = new HashMap<>();

	public RitualCulling() {
		super(Constants.Rituals.CULLING, 0, 50000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.CULLING);

		addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), 21));

		setMaximumVolumeAndDistanceOfRange(ALTAR_RANGE, 0, 10, 15);
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 15, 15);

	}

	public double smallGauss(double d) {
		Random myRand = new Random();
		return (myRand.nextFloat() - 0.5D) * d;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		willBuffer = tag.getDouble(Constants.NBT.CULLING_BUFFER_WILL);

	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setDouble(Constants.NBT.CULLING_BUFFER_WILL, willBuffer);

	}

	@Override
	public boolean activateRitual(IMasterRitualStone ritualStone, EntityPlayer player, UUID owner) {
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

		int               currentEssence = network.getCurrentEssence();
		World             world          = ritualStone.getWorldObj();
		World             soundSource    = ritualStone.getWorldObj();
		EnumDemonWillType type           = EnumDemonWillType.DESTRUCTIVE;
		BlockPos          pos            = ritualStone.getBlockPos();
		double            currentAmount  = WorldDemonWillHandler.getCurrentWill(world, pos, type);


		TileAltar tileAltar = AnimusUtil.getNearbyAltar(world, ritualStone.getBlockRange(ALTAR_RANGE), pos, altarOffsetPos);
		if (tileAltar == null)
			return;
		altarOffsetPos = tileAltar.getPos();

		AreaDescriptor damageRange = ritualStone.getBlockRange(EFFECT_RANGE);
		AxisAlignedBB  range       = damageRange.getAABB(pos);

		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, range);

		int entityCount = 0;

		if (currentEssence < this.getRefreshCost() * list.size()) {
			network.causeNausea();
		} else {
			for (EntityLivingBase livingEntity : list) {
				/*if (ConfigHandler.wellOfSufferingBlacklist.contains(livingEntity.getClass().getSimpleName()))
					continue;*/

				if (livingEntity instanceof EntityPlayer && livingEntity.getHealth() > 4)
					continue;

				Collection<PotionEffect> effect = livingEntity.getActivePotionEffects(); // Disallows cursed earth spawned mobs

				if (effect.isEmpty()) {
					float    damage = 0;
					BlockPos at     = livingEntity.getPosition();
					soundSource = livingEntity.world;
					boolean isNonBoss = livingEntity.isNonBoss();

					if (livingEntity.getName().contains("Gaia"))
						continue;

					livingEntity.setSilent(true); // The screams of the weak fall on deaf ears.

					damage = Integer.MAX_VALUE;

					if (AnimusConfig.rituals.killWither && !isNonBoss && currentAmount > 99
							&& (currentEssence >= 25000 + (this.getRefreshCost() * list.size()))) { // Special case for Bosses, they require maxed vengeful will and 50k LP per kill
						livingEntity.setEntityInvulnerable(false);
						if (livingEntity instanceof EntityWither) {
							EntityWither EW = (EntityWither) livingEntity;
							EW.setInvulTime(0);
						}
					}

					result = (livingEntity.attackEntityFrom(culled, damage));

					if (result) {
						entityCount++;
						tileAltar.sacrificialDaggerCall(RitualCulling.amount, true);

						if (!isNonBoss) {
							network.syphon(new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_CULLING), 25000));
						} else {
							double modifier = .5;
							if (livingEntity instanceof EntityAnimal) {//&& !livingEntity.isCollided
								modifier = 2;
							}
							willBuffer += modifier * Math.min(15.00, livingEntity.getMaxHealth());
						}
						if (world.isRemote) {
							for (int i = 0; i < rand.nextInt(4); i++)
								world.spawnParticle(EnumParticleTypes.PORTAL, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + .5,
										(rand.nextDouble() - 0.5D) * 2.0D, -rand.nextDouble(), (rand.nextDouble() - 0.5D) * 2.0D);
						}
						soundSource.playSound(null, at, AnimusSoundEventHandler.ghostly, SoundCategory.BLOCKS, 1F,
								1F);
					}
				}

			}

			network.syphon(new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_CULLING), getRefreshCost() * entityCount));
			double drainAmount = Math.min(maxWill - currentAmount, Math.min(entityCount / 2, 10));

			if (rand.nextInt(30) == 0) { // 3% chance per cycle to generate destructive will
				double filled = WorldDemonWillHandler.fillWillToMaximum(world, pos, type, drainAmount, maxWill, false);
				if (filled > 0)
					WorldDemonWillHandler.fillWillToMaximum(world, pos, type, filled, maxWill, true);
			}
		}

	}

	@Override
	public int getRefreshCost() {
		return 75;
	}

	@Override
	public int getRefreshTime() {
		return 25;
	}

	@Override
	public void gatherComponents(Consumer<RitualComponent> components) {
		components.accept(new RitualComponent(new BlockPos(1, 0, 1), EnumRuneType.FIRE));
		components.accept(new RitualComponent(new BlockPos(-1, 0, 1), EnumRuneType.FIRE));
		components.accept(new RitualComponent(new BlockPos(1, 0, -1), EnumRuneType.FIRE));
		components.accept(new RitualComponent(new BlockPos(-1, 0, -1), EnumRuneType.FIRE));
		components.accept(new RitualComponent(new BlockPos(2, -1, 2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(2, -1, -2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-2, -1, 2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-2, -1, -2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(0, -1, 2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(2, -1, 0), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(0, -1, -2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-2, -1, 0), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-3, -1, -3), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(3, -1, -3), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-3, -1, 3), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(3, -1, 3), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(2, -1, 4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(4, -1, 2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-2, -1, 4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(4, -1, -2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(2, -1, -4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-4, -1, 2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-2, -1, -4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-4, -1, -2), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(1, 0, 4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(4, 0, 1), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(1, 0, -4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-4, 0, 1), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-1, 0, 4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(4, 0, -1), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-1, 0, -4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-4, 0, -1), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(4, 1, 0), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(0, 1, 4), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-4, 1, 0), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(0, 1, -4), EnumRuneType.DUSK));
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualCulling();
	}

}
