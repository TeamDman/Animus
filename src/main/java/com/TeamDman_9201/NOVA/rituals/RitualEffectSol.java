package com.teamdman_9201.nova.rituals;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.ArrayList;
import java.util.List;

import WayofTime.alchemicalWizardry.api.alchemy.energy.ReagentRegistry;
import WayofTime.alchemicalWizardry.api.rituals.IMasterRitualStone;
import WayofTime.alchemicalWizardry.api.rituals.RitualComponent;
import WayofTime.alchemicalWizardry.api.rituals.RitualEffect;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import WayofTime.alchemicalWizardry.common.spell.complex.effect.SpellHelper;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualEffectSol extends RitualEffect {
    public int reagentDrain = 5;

    @Override
    public void performEffect(IMasterRitualStone ritualStone) {
        String owner          = ritualStone.getOwner();
        World  world          = ritualStone.getWorld();
        int    x              = ritualStone.getXCoord();
        int    y              = ritualStone.getYCoord();
        int    z              = ritualStone.getZCoord();
        int    currentEssence = SoulNetworkHandler.getCurrentEssence(owner);
        if (currentEssence < this.getCostPerRefresh()) {
            EntityPlayer entityOwner = SpellHelper.getPlayerForUsername(owner);
            if (entityOwner == null) {
                return;
            }
            SoulNetworkHandler.causeNauseaToPlayer(owner);
        } else {
            for (int i = 0; i < 10; i++) {
                SpellHelper.sendIndexedParticleToAllAround(world, x, y, z, 20, world.provider
                        .dimensionId, 3, x, y, z);
            }
            Block source = world.getBlock(x, y + 1, z);
            int meta = world.getBlockMetadata(x, y + 1, z);
            int radius = this.canDrainReagent(ritualStone, ReagentRegistry.virtusReagent,
                    reagentDrain, false) ? 5 : 1;
            if (source == Blocks.air)
                return;
            if (world.getBlock(x, y + 1, z).getLightValue() > 0) {
                int[] pos = getNextBlock(world, x, z, radius);
                if (pos == null) {
                    source.dropBlockAsItem(world, x, y + 1, z, meta, 25);
                } else {
                    world.setBlock(pos[0], pos[1] + 1, pos[2], source);
                    this.canDrainReagent(ritualStone, ReagentRegistry.virtusReagent,
                            reagentDrain, true);
                }
            }
            world.setBlockToAir(x, y + 1, z);
        }
    }

    @Override
    public int getCostPerRefresh() {
        return 10;
    }

    @Override
    public List<RitualComponent> getRitualComponentList() {
        ArrayList<RitualComponent> ritualBlocks = new ArrayList();
        ritualBlocks.add(new RitualComponent(1, 0, 0, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(-1, 0, 0, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(0, 0, 1, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(0, 0, -1, RitualComponent.FIRE));
        ritualBlocks.add(new RitualComponent(1, 0, 1, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-1, 0, 1, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(-1, 0, -1, RitualComponent.EARTH));
        ritualBlocks.add(new RitualComponent(1, 0, -1, RitualComponent.EARTH));

        ritualBlocks.add(new RitualComponent(2, 1, 2, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(-2, 1, 2, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(-2, 1, -2, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(2, 1, -2, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(2, 1, 1, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(-2, 1, 1, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(-2, 1, -1, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(2, 1, -1, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(1, 1, 2, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(-1, 1, 2, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(-1, 1, -2, RitualComponent.AIR));
        ritualBlocks.add(new RitualComponent(1, 1, -2, RitualComponent.AIR));
        return ritualBlocks;
    }

    public int[] getNextBlock(World world, int ritualX, int ritualZ, int radius) {
        int            startChunkX = ritualX >> 4;
        int            startChunkZ = ritualZ >> 4;
        IChunkProvider provider    = world.getChunkProvider();
        for (int chunkX = startChunkX - radius; chunkX <= startChunkX + radius; chunkX++) {
            for (int chunkZ = startChunkZ - radius; chunkZ <= startChunkZ + radius; chunkZ++) {
                provider.loadChunk(chunkX, chunkZ);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 254; y > 1; y--) {
                            int wx = chunkX * 16 + x;
                            int wz = chunkZ * 16 + z;
                            if (checkSpot(world, new int[]{wx, y, wz}))
                                return new int[]{wx, y, wz};
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean checkSpot(World world, int[] pos) {
        int x = pos[0];
        int y = pos[1];
        int z = pos[2];
        if (!world.isAirBlock(x, y, z)) {
            if (world.isAirBlock(x, y + 1, z) && world.getSavedLightValue(EnumSkyBlock.Block, x,
                    y + 1, z) < 8) {
                if (world.doesBlockHaveSolidTopSurface(world, x, y, z)) {
                    return true;
                }
            }
        }
        return false;
    }
}

