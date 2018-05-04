package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.ritual.EnumRuneType;
import WayofTime.bloodmagic.ritual.IMasterRitualStone;
import WayofTime.bloodmagic.ritual.Ritual;
import WayofTime.bloodmagic.ritual.RitualComponent;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.Animus;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.function.Consumer;
import com.teamdman.animus.Constants;
/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualPeace extends Ritual {
	public RitualPeace() {
		super("ritualPeace", 0, 5000, "ritual." + Constants.Mod.MODID + ".peace");
	}
	EntityList.EntityEggInfo[] targets;

	@Override
	public boolean activateRitual(IMasterRitualStone masterRitualStone, EntityPlayer player, UUID owner) {
		try {
			targets = EntityList.ENTITY_EGGS.values().stream()
					.filter(e -> e.spawnedID.getResourceDomain().equals("minecraft"))
					.filter(e -> EntityList.createEntityByIDFromName(e.spawnedID, masterRitualStone.getWorldObj()) != null)
					.filter(e -> !EntityList.createEntityByIDFromName(e.spawnedID, masterRitualStone.getWorldObj()).isCreatureType(EnumCreatureType.MONSTER,false))
					.toArray(EntityList.EntityEggInfo[]::new);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Peace ritual creation failed. Entity List:");
			EntityList.ENTITY_EGGS.values().forEach(System.out::println);
			return false;
		}
	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World       world     = masterRitualStone.getWorldObj();
		SoulNetwork network   = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		BlockPos    masterPos = masterRitualStone.getBlockPos();

		if (!world.isRemote) {
			Entity mob;
			mob = EntityList.createEntityByIDFromName(targets[world.rand.nextInt(targets.length)].spawnedID, world);
			if (mob == null) // _should_ never happen
				masterRitualStone.stopRitual(BreakType.DEACTIVATE);
			for (int i = 0; i < 16; i++) {
				mob.setPosition(masterPos.getX() + world.rand.nextInt(8) - 4, masterPos.getY() + 1, masterPos.getZ() + world.rand.nextInt(8) - 4);
				if (!world.isAirBlock(mob.getPosition()))
					break;
			}
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
		addRune(components,4, 0, 0, EnumRuneType.EARTH);
		addRune(components,4, 1, 0, EnumRuneType.EARTH);
		addRune(components,4, 2, 0, EnumRuneType.WATER);
		addRune(components,0, 1, 4, EnumRuneType.EARTH);
		addRune(components,0, 2, 4, EnumRuneType.EARTH);
		addRune(components,0, 3, 4, EnumRuneType.WATER);
		addRune(components,-4, 2, 0, EnumRuneType.EARTH);
		addRune(components,-4, 3, 0, EnumRuneType.EARTH);
		addRune(components,-4, 4, 0, EnumRuneType.WATER);
		addRune(components,0, 3, -4, EnumRuneType.EARTH);
		addRune(components,0, 4, -4, EnumRuneType.EARTH);
		addRune(components,0, 5, -4, EnumRuneType.WATER);
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualPeace();
	}

}