package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.arsnouveau.SourceJarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;

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

        if (!SourceJarHelper.isSourceJar(serverLevel, masterPos.above())) {
            return;
        }

        int altarSearchRadius = AnimusConfig.rituals.arsVitaeAltarRange.get();
        AraVitaeTile altar = findNearbyAltar(serverLevel, masterPos, altarSearchRadius);

        if (altar == null) {
            return;
        }

        int penaltyRadius = AnimusConfig.rituals.arsVitaePenaltyRadius.get();
        int nearbyRituals = countNearbyMasterRitualStones(serverLevel, altar.getBlockPos(), penaltyRadius);

        int baseConversion = AnimusConfig.rituals.sourceVitaeumBaseConversion.get();
        int conversionRate = baseConversion * (int) Math.pow(2, nearbyRituals);

        int availableEV = altar.getCurrentBlood();

        if (availableEV < conversionRate) {
            return;
        }

        int sourcePerCycle = AnimusConfig.rituals.arsVitaeSourcePerCycle.get();
        int affordableSource = Math.min(sourcePerCycle, availableEV / conversionRate);

        if (affordableSource <= 0) {
            return;
        }

        int sourceAdded = SourceJarHelper.addSourceToJarAbove(serverLevel, masterPos, affordableSource);

        if (sourceAdded <= 0) {
            return;
        }

        altar.drainMainTank(sourceAdded * conversionRate);
    }

    private AraVitaeTile findNearbyAltar(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        )) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AraVitaeTile altar) {
                return altar;
            }
        }
        return null;
    }

    private int countNearbyMasterRitualStones(ServerLevel level, BlockPos altarPos, int radius) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
            altarPos.offset(-radius, -radius, -radius),
            altarPos.offset(radius, radius, radius)
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
