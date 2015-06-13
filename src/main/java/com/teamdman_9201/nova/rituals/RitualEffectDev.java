package com.teamdman_9201.nova.rituals;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import WayofTime.alchemicalWizardry.api.rituals.IMasterRitualStone;
import WayofTime.alchemicalWizardry.api.rituals.IRitualStone;
import WayofTime.alchemicalWizardry.api.rituals.RitualComponent;
import WayofTime.alchemicalWizardry.api.rituals.RitualEffect;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualEffectDev extends RitualEffect {

    @Override
    public int getCostPerRefresh() {
        return 10;
    }

    @Override
    public List<RitualComponent> getRitualComponentList() {
        ArrayList<RitualComponent> ritualBlocks = new ArrayList();
        ritualBlocks.add(new RitualComponent(0, -1, 0, RitualComponent.DUSK));
        return ritualBlocks;
    }

    @Override
    public void performEffect(IMasterRitualStone ritualStone) {

    }

    @Override
    public boolean startRitual(IMasterRitualStone ritualStone, EntityPlayer player) {
        int   x     = ritualStone.getXCoord();
        int   y     = ritualStone.getYCoord();
        int   z     = ritualStone.getZCoord();
        World world = ritualStone.getWorld();
        for (int xPos = -50; xPos < 50; ++xPos) {
            for (int yPos = 0; yPos < 256; yPos++) {
                for (int zPos = -50; zPos < 50; ++zPos) {
                    if (xPos == 0 && yPos == y-1 && zPos == 0)
                        continue;
                    if (world.getBlock(x + xPos, yPos, z + zPos) instanceof IRitualStone) {
                        String type;
                        int meta = world.getBlockMetadata(x + xPos, yPos, z + zPos);
                        switch (meta) {
                            case 0:
                                type = "BLANK";
                                break;
                            case 1:
                                type = "WATER";
                                break;
                            case 2:
                                type = "FIRE";
                                break;
                            case 3:
                                type = "EARTH";
                                break;
                            case 4:
                                type = "AIR";
                                break;
                            case 5:
                                type = "DUSK";
                                break;
                            default:
                                type = meta + "";
                                break;
                        }
                        System.out.println("ritualBlocks.add(new RitualComponent(" + xPos + "," +
                                "" + (yPos - y) + "," + zPos + ", RitualComponent." + type + "));");
                    }
                }
            }
        }
        return true;
    }
}

