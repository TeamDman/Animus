package com.teamdman.animus.rituals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.teamdman.animus.Animus;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import WayofTime.bloodmagic.ConfigHandler;
import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.tile.TileAltar;

public class RitualCulling extends Ritual {
	public static final String EFFECT_RANGE = "effect";

	public RitualCulling() {
		super("ritualCulling", 2, 50000, "ritual." + Animus.MODID + ".cullingritual");
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));

		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);

	}

	DamageSource culled = new DamageSource("animus.absolute").setDamageAllowedInCreativeMode().setDamageBypassesArmor()
			.setDamageIsAbsolute();

	public int reagentDrain = 2;
	public boolean result = false;
	public static final int timeDelay = 20;
	public static final int amount = 200;

	@Override
	public boolean activateRitual(IMasterRitualStone ritualStone, EntityPlayer player, String owner) {
		Random itemRand = new Random();
		double xCoord, yCoord, zCoord;

		xCoord = ritualStone.getBlockPos().getX();
		yCoord = ritualStone.getBlockPos().getY();
		zCoord = ritualStone.getBlockPos().getZ();

		player.worldObj.addWeatherEffect(new EntityLightningBolt(player.worldObj, xCoord + itemRand.nextInt(64) - 32,
				yCoord + itemRand.nextInt(8) - 8, zCoord + itemRand.nextInt(64) - 32, false));

		return true;
	}

	@Override
	public void performRitual(IMasterRitualStone ritualStone) {
		SoulNetwork network = NetworkHelper.getSoulNetwork(ritualStone.getOwner());
		int currentEssence = network.getCurrentEssence();

		World world = ritualStone.getWorldObj();
		int x = ritualStone.getBlockPos().getX();
		int y = ritualStone.getBlockPos().getY();
		int z = ritualStone.getBlockPos().getZ();
		;

		TileAltar tileAltar = null;
		boolean testFlag = false;

		if (world.getWorldTime() % RitualCulling.timeDelay != 0) {
			return;
		}
		for (int i = -5; i <= 5; i++) {
			for (int j = -5; j <= 5; j++) {
				for (int k = -10; k <= 10; k++) {
					if (world.getTileEntity(new BlockPos(x + i, y + k, z + j)) instanceof TileAltar) {
						tileAltar = (TileAltar) world.getTileEntity(new BlockPos(x + i, y + k, z + j));
						testFlag = true;
					}
				}
			}
		}

		if (!testFlag) {
			return;
		}
		int d0 = 10;
		int vertRange = 10;
		AxisAlignedBB axisalignedbb = new AxisAlignedBB((double) x, (double) y, (double) z, (double) (x + 1),
				(double) (y + 1), (double) (z + 1)).expand(d0, vertRange, d0);

		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, axisalignedbb);

		int entityCount = 0;

		if (currentEssence < this.getRefreshCost() * list.size()) {
			network.causeNausea();
		} else {
			for (EntityLivingBase livingEntity : list) {
				if (livingEntity.isNonBoss()
						|| (ConfigHandler.wellOfSufferingBlacklist.contains(livingEntity.getClass().getSimpleName()))) {
					continue;
				}
				if (livingEntity instanceof EntityPlayer && livingEntity.getHealth() > 4)
					continue;

				Collection<PotionEffect> effect = livingEntity.getActivePotionEffects(); // Cursed
																							// earth
																							// boosted

				if (effect == null) {
					int p = 0;
					for (p = 0; p < 6; p++)

						if (network.getPlayer() != null)
							result = livingEntity.attackEntityFrom(DamageSource.causePlayerDamage(network.getPlayer()),
									livingEntity.getMaxHealth() * 3);
					if (result != true)
						result = (livingEntity.attackEntityFrom(culled, livingEntity.getMaxHealth() * 2));

					if (result != false) {
						entityCount++;
						if (effect == null)
							tileAltar.sacrificialDaggerCall(RitualCulling.amount, true);
					}
				}
			}

			network.syphon(getRefreshCost() * entityCount);

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
	public int getRefreshCost() {
		return 75;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public ArrayList<RitualComponent> getComponents() {
		@SuppressWarnings("unchecked")
		ArrayList<RitualComponent> ritualBlocks = new ArrayList();
		;

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