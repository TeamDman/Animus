package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.arsnouveau.SourceJarHelper;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.ritual.*;

import java.util.function.Consumer;

public class RitualArsVitae extends Ritual {

    public RitualArsVitae() {
        super(
            Constants.Rituals.ARS_VITAE,
            0,
            10000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ARS_VITAE
        );
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!CompatHandler.isArsNouveauLoaded()) {
            return;
        }

        int jarSpace = SourceJarHelper.getFreeSpaceAbove(serverLevel, masterPos);

        if (jarSpace <= 0) {
            return;
        }

        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);

        if (network == null) {
            return;
        }

        int penaltyRadius = AnimusConfig.rituals.arsVitaePenaltyRadius.get();
        int nearbyRituals = countNearbyMasterRitualStones(serverLevel, masterPos, penaltyRadius);

        int baseConversion = AnimusConfig.rituals.sourceVitaeumBaseConversion.get();
        int conversionRate = baseConversion * (int) Math.pow(2, nearbyRituals);

        int availableEV = network.getCurrentEV();

        if (availableEV < conversionRate) {
            return;
        }

        int sourcePerCycle = AnimusConfig.rituals.arsVitaeSourcePerCycle.get();
        int affordableSource = Math.min(Math.min(sourcePerCycle, jarSpace), availableEV / conversionRate);

        if (affordableSource <= 0) {
            return;
        }

        int sourceAdded = SourceJarHelper.addSourceToJarAbove(serverLevel, masterPos, affordableSource);

        if (sourceAdded <= 0) {
            return;
        }

        network.syphon(AnimaTicket.create(sourceAdded * conversionRate));
    }

    private int countNearbyMasterRitualStones(ServerLevel level, BlockPos center, int radius) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        )) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IMasterRitualStone) {
                count++;
            }
        }
        return Math.max(0, count - 1);
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.arsVitaeRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, -2, 0, -1, EnumRuneType.AIR);
        addRune(components, -2, 0, 1, EnumRuneType.AIR);
        addRune(components, -1, 0, -2, EnumRuneType.AIR);
        addRune(components, -1, 0, -1, EnumRuneType.AIR);
        addRune(components, -1, 0, 0, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.AIR);
        addRune(components, -1, 0, 2, EnumRuneType.AIR);
        addRune(components, 0, 0, -1, EnumRuneType.AIR);
        addRune(components, 0, 0, 1, EnumRuneType.FIRE);
        addRune(components, 1, 0, -2, EnumRuneType.AIR);
        addRune(components, 1, 0, -1, EnumRuneType.AIR);
        addRune(components, 1, 0, 0, EnumRuneType.WATER);
        addRune(components, 1, 0, 1, EnumRuneType.AIR);
        addRune(components, 1, 0, 2, EnumRuneType.AIR);
        addRune(components, 2, 0, -1, EnumRuneType.AIR);
        addRune(components, 2, 0, 1, EnumRuneType.AIR);
        addRune(components, -2, 1, -1, EnumRuneType.EARTH);
        addRune(components, -2, 1, 1, EnumRuneType.EARTH);
        addRune(components, -1, 1, -2, EnumRuneType.AIR);
        addRune(components, -1, 1, 2, EnumRuneType.FIRE);
        addRune(components, 1, 1, -2, EnumRuneType.AIR);
        addRune(components, 1, 1, 2, EnumRuneType.FIRE);
        addRune(components, 2, 1, -1, EnumRuneType.WATER);
        addRune(components, 2, 1, 1, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualArsVitae();
    }

}
