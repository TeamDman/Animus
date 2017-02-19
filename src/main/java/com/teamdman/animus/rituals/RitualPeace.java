package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.api.ritual.EnumRuneType;
import WayofTime.bloodmagic.api.ritual.IMasterRitualStone;
import WayofTime.bloodmagic.api.ritual.Ritual;
import WayofTime.bloodmagic.api.ritual.RitualComponent;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import com.teamdman.animus.Animus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualPeace extends Ritual {
	public RitualPeace() {
		super("ritualPeace", 0, 5000, "ritual." + Animus.MODID + ".peace");

	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World world = masterRitualStone.getWorldObj();
		SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		BlockPos masterPos = masterRitualStone.getBlockPos();

		if (!masterRitualStone.getWorldObj().isRemote) {
			Entity mob;
			do {
				String id = ((EntityList.EntityEggInfo) EntityList.ENTITY_EGGS.values().toArray()[world.rand.nextInt(EntityList.ENTITY_EGGS.values().toArray().length - 1)]).spawnedID;
				mob = EntityList.createEntityByIDFromName(id, world);
			} while (mob.isCreatureType(EnumCreatureType.MONSTER, false));
			do {
				mob.setPosition(masterPos.getX() + world.rand.nextInt(8) - 4, masterPos.getY() + 1, masterPos.getZ() + world.rand.nextInt(8) - 4);
			} while (!world.isAirBlock(mob.getPosition()));
			world.spawnEntity(mob);
			world.playSound(null, mob.getPosition(), SoundEvents.BLOCK_SNOW_STEP, SoundCategory.BLOCKS, 1, 1);
			network.syphon(getRefreshCost());
		}
	}

	@Override
	public int getRefreshCost() {
		return 1000;
	}

	@Override
	public int getRefreshTime() {
		return 400;
	}

	@Override
	public ArrayList<RitualComponent> getComponents() {
		ArrayList<RitualComponent> components = new ArrayList<RitualComponent>();
		this.addParallelRunes(components, 4, 0, EnumRuneType.EARTH);
		this.addCornerRunes(components, 4, 0, EnumRuneType.EARTH);
		this.addOffsetRunes(components, 4, 1, 0, EnumRuneType.EARTH);
		this.addOffsetRunes(components, 4, 2, 0, EnumRuneType.EARTH);

		this.addCornerRunes(components, 1, 0, EnumRuneType.WATER);
		this.addParallelRunes(components, 1, 0, EnumRuneType.WATER);
		return components;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualPeace();
	}

}