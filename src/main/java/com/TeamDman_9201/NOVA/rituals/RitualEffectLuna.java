package com.teamdman_9201.nova.rituals;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
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
public class RitualEffectLuna extends RitualEffect {
    public int reagentDrain = 5;
    ArrayList<int[]> lightLocations;

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
            int radius = this.canDrainReagent(ritualStone, ReagentRegistry.virtusReagent,
                    reagentDrain, false) ? 5 : 1;
            int[] pos = getNextBlock(world, x, z, radius);
            if (pos != null) {
                Block light = world.getBlock(pos[0], pos[1] + 1, pos[2]);
                int meta = world.getBlockMetadata(pos[0], pos[1] + 1, pos[2]);
                light.dropBlockAsItem(world, x, y + 1, z, meta, 25);
                world.setBlockToAir(pos[0], pos[1] + 1, pos[2]);
                this.canDrainReagent(ritualStone, ReagentRegistry.virtusReagent, reagentDrain,
                        true);
            }
        }
    }

    @Override
    public int getCostPerRefresh() {
        return 10;
    }

    @Override
    public List<RitualComponent> getRitualComponentList() {
        ArrayList<RitualComponent> ritualBlocks = new ArrayList();
        ritualBlocks.add(new RitualComponent(1, 0, 0, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-1, 0, 0, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(0, 0, 1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(0, 0, -1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(1, 0, 1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-1, 0, 1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-1, 0, -1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(1, 0, -1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(2, 1, 2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-2, 1, 2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-2, 1, -2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(2, 1, -2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(2, 1, 1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-2, 1, 1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-2, 1, -1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(2, 1, -1, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(1, 1, 2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-1, 1, 2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(-1, 1, -2, RitualComponent.DUSK));
        ritualBlocks.add(new RitualComponent(1, 1, -2, RitualComponent.DUSK));
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
                            if (world.getBlock(wx, y, wz).getLightValue() > 0)
                                return new int[]{wx, y, wz};
                        }
                    }
                }
            }
        }
        return null;
    }

}

