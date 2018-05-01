package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.ritual.Ritual;
import WayofTime.bloodmagic.ritual.Ritual;
import WayofTime.bloodmagic.ritual.Ritual;
import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.Animus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.function.Consumer;

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
				ResourceLocation id = ((EntityList.EntityEggInfo) EntityList.ENTITY_EGGS.values().toArray()[world.rand.nextInt(EntityList.ENTITY_EGGS.values().toArray().length - 1)]).spawnedID;
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
	public void gatherComponents(Consumer<RitualComponent> components) {
		components.accept(new RitualComponent( new BlockPos(0,4, 0), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(0, 4, 0), EnumRuneType.EARTH));
		components.accept(new RitualComponent( new BlockPos(4, 1, 0), EnumRuneType.EARTH));
		components.accept(new RitualComponent( new BlockPos(4, 2, 0), EnumRuneType.EARTH));

		this.addCornerRunes(components, 1, 0, EnumRuneType.WATER);
	}



	@Override
	public Ritual getNewCopy() {
		return new RitualPeace();
	}

}