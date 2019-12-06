package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.tile.TileAltar;
import WayofTime.bloodmagic.util.helper.NetworkHelper;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.common.util.AnimusUtil;
import com.teamdman.animus.handlers.AnimusSoundEventHandler;
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import thaumcraft.api.aura.AuraHelper;
import thaumcraft.common.lib.SoundsTC;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;


@RitualRegister(Constants.Rituals.ELDRITCH)
public class RitualEldritchWill extends Ritual {
	
	public static final String   ALTAR_RANGE    = "altar";
	public static final String   EFFECT_RANGE   = "effect";
	public              BlockPos altarOffsetPos = new BlockPos(0, 0, 0);
	public              double   will           = 100;
	public final        int    maxWill      = 100;	
	public int willRadius = AnimusConfig.rituals.willRadius;

	
	public RitualEldritchWill() {
		super(Constants.Rituals.ELDRITCH, 0, 5000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ELDRITCH);

		addBlockRange(ALTAR_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-5, -10, -5), 11, 21, 11));
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), 24));
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 1, 1, 1);
		if (willRadius > 1)
				willRadius = 1;
		else if (willRadius < 0)
				willRadius = 0;
	}

	
	

	public void performRitual(IMasterRitualStone ritualStone) {
		World             world  = ritualStone.getWorldObj();
		Random            random = world.rand;
		BlockPos          pos    = ritualStone.getBlockPos();
		EnumDemonWillType type   = EnumDemonWillType.DEFAULT;
		will = WorldDemonWillHandler.getCurrentWill(world, pos, type);

		SoulNetwork network        = NetworkHelper.getSoulNetwork(ritualStone.getOwner());
		int         currentEssence = network.getCurrentEssence();

		if (!Animus.thaumcraftLoaded)
			return;
		
		if (!ritualStone.getWorldObj().isRemote) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}
			float flux = AuraHelper.getFlux(world, pos);
			int fluxConfig = AnimusConfig.rituals.fluxDrainMax;
			if (fluxConfig < 1)
				fluxConfig = 1;
			float reduction = random.nextInt(fluxConfig);
			if (reduction > flux)
				reduction = flux;
			
			
			List<BlockPos> UpdateChunks = new ArrayList<BlockPos>();
			Chunk chunk = world.getChunk(pos);
			BlockPos centeredPos = new BlockPos(getCenterX(chunk),1,getCenterZ(chunk));
			UpdateChunks.add(centeredPos);
			
			//Get list of centered chunk block positions that we'll run the aura helper on
			switch (willRadius) {
				case 1:
					UpdateChunks.add((new BlockPos((getCenterX(chunk,-1)),4,getCenterZ(chunk,-1))));
					UpdateChunks.add((new BlockPos((getCenterX(chunk,-1)),4,getCenterZ(chunk))));
					UpdateChunks.add((new BlockPos((getCenterX(chunk,-1)),4,getCenterZ(chunk,1))));
					UpdateChunks.add((new BlockPos((getCenterX(chunk)),4,getCenterZ(chunk,1))));
					UpdateChunks.add((new BlockPos((getCenterX(chunk)),4,getCenterZ(chunk,-1))));
					UpdateChunks.add((new BlockPos((getCenterX(chunk,1)),4,getCenterZ(chunk,-1))));
					UpdateChunks.add((new BlockPos((getCenterX(chunk,1)),4,getCenterZ(chunk))));
					UpdateChunks.add((new BlockPos((getCenterX(chunk,1)),4,getCenterZ(chunk,1))));
					break;
				default:
					break;
			}
					
			//run the aura helper on those blocks
			for (int z = 0; z < UpdateChunks.size(); z++) {
				//System.out.println("Running Aura helper on Block : " + UpdateChunks.get(z).toString());
				BlockPos updatePos = UpdateChunks.get(z);	    
				
				AuraHelper.drainFlux(world, updatePos, reduction, false);
				AuraHelper.polluteAura(world, updatePos, 0, true);				
			}
			

			
			int fluxMulti = AnimusConfig.rituals.fluxToWillConversionMultiplier;
			if (fluxMulti < 0)
				fluxMulti = 0;
			
			double filled = WorldDemonWillHandler.fillWillToMaximum(world, pos, type, reduction*fluxMulti, maxWill, false);
			if (filled > 0)
				WorldDemonWillHandler.fillWillToMaximum(world, pos, type, filled, maxWill, true);
			
			
			network.syphon(new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_ELDRITCH), this.getRefreshCost()));
		}

	}

	@Override
	public int getRefreshCost() {
		if (!Animus.thaumcraftLoaded)
			return 0;
		else
			return AnimusConfig.rituals.eldritchWillCost;
	}
	
	@Override
	public int getRefreshTime() {
		if (!Animus.thaumcraftLoaded)
			return 10000;
		else
			return AnimusConfig.rituals.eldritchWillSpeed;
	}	

	@Override
	public void gatherComponents(Consumer<RitualComponent> components) {
		components.accept(new RitualComponent(new BlockPos(-2, 1, -2), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(-2, 1, 0), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(-2, 1, 2), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(-1, 0, -1), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(-1, 0, 1), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(0, 1, -2), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(0, 1, 2), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(1, 0, -1), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(0, -1, 0), EnumRuneType.FIRE));
		components.accept(new RitualComponent(new BlockPos(1, 0, 1), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(2, 1, -2), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(2, 1, 0), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(2, 1, 2), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(3, 1, 3), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(3, 1, -3), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-3, 1, -3), EnumRuneType.DUSK));
		components.accept(new RitualComponent(new BlockPos(-3, 1, 3), EnumRuneType.DUSK));
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualEldritchWill();
	}

	public int getCenterX(Chunk chunk) {

		return (chunk.x << 4) + 8;
	}
	public int getCenterZ(Chunk chunk) {

		return (chunk.z << 4) + 8;
	}
	public int getCenterX(Chunk chunk, int offset) {

		return ((chunk.x << 4) + 8) + (offset * 16);
	}
	public int getCenterZ(Chunk chunk,int offset) {

		return ((chunk.z << 4) + 8) + (offset * 16);
	}	
}
