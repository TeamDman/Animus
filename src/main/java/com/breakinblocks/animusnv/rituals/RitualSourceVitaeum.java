package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.util.SourceJarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;

import java.util.function.Consumer;

/**
 * Ritual of Source Vitaeum - Converts Ars Nouveau Source into NeoVitae EV
 *
 * This ritual creates a bridge between Ars Nouveau's Source system and NeoVitae's EV system,
 * allowing hybrid players to convert excess Source into EV at a configurable exchange rate.
 *
 * Features:
 * - Requires a Source Jar directly above the Master Ritual Stone (at y+1)
 * - Searches for Ara Vitaes within configurable radius (default 8 blocks)
 * - Drains Source from the jar above the ritual
 * - Base conversion: 10 Source to 1 EV (configurable)
 * - Penalty system: Each nearby Master Ritual Stone doubles the conversion cost
 * - Respects altar speed runes, transfer limits, and dislocation runes
 * - Optional integration: Only active when Ars Nouveau is installed
 *
 * Activation Cost: 10000 EV
 * Refresh Cost: 0 EV (conversion happens via Source drain)
 * Refresh Time: Based on altar speed (default 40 ticks / 2 seconds)
 */
public class RitualSourceVitaeum extends Ritual {

    public RitualSourceVitaeum() {
        super(
            Constants.Rituals.SOURCE_VITAEUM,
            0,
            10000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SOURCE_VITAEUM
        );
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!CompatHandler.isArsNouveauLoaded()) {
            return;
        }

        int altarSearchRadius = AnimusConfig.rituals.sourceVitaeumAltarRange.get();
        AraVitaeTile altar = findNearbyAltar(serverLevel, masterPos, altarSearchRadius);

        if (altar == null) {
            return;
        }

        int penaltyRadius = AnimusConfig.rituals.sourceVitaeumPenaltyRadius.get();
        int nearbyRituals = countNearbyMasterRitualStones(serverLevel, altar.getBlockPos(), penaltyRadius);

        // Each nearby ritual stone doubles the conversion cost
        int baseConversion = AnimusConfig.rituals.sourceVitaeumBaseConversion.get();
        int conversionRate = SourceJarHelper.conversionRate(baseConversion, nearbyRituals);

        int sourcePerCycle = AnimusConfig.rituals.sourceVitaeumSourcePerCycle.get();

        int availableSpace = Math.max(0, altar.getMainCapacity() - altar.getCurrentBlood());
        double multiplier = Math.max(1, 1 + altar.getSacrificeBonus());
        int maxBaseEV = (int) Math.floor(availableSpace / multiplier);
        int availableSource = SourceJarHelper.getSourceAmount(serverLevel, masterPos.above());
        int evToAdd = Math.min(maxBaseEV, Math.min(sourcePerCycle, availableSource) / conversionRate);
        if (evToAdd <= 0) {
            return;
        }

        int sourceDrained = SourceJarHelper.drainSourceFromJarAbove(serverLevel, masterPos, evToAdd * conversionRate);
        int convertedEV = sourceDrained / conversionRate;
        int remainder = sourceDrained % conversionRate;
        if (remainder > 0) {
            SourceJarHelper.addSourceToJarAbove(serverLevel, masterPos, remainder);
        }
        if (convertedEV > 0) {
            // Second param (true) makes it respect altar speed and dislocation runes
            altar.addSacrificeEV(convertedEV, true);
        }
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
        return AnimusConfig.rituals.sourceVitaeumRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, -2, 0, -1, EnumRuneType.AIR);
        addRune(components, -2, 0, 1, EnumRuneType.AIR);
        addRune(components, -1, 0, -2, EnumRuneType.AIR);
        addRune(components, -1, 0, -1, EnumRuneType.AIR);
        addRune(components, -1, 0, 0, EnumRuneType.WATER);
        addRune(components, -1, 0, 1, EnumRuneType.AIR);
        addRune(components, -1, 0, 2, EnumRuneType.AIR);
        addRune(components, 0, 0, -1, EnumRuneType.FIRE);
        addRune(components, 0, 0, 1, EnumRuneType.AIR);
        addRune(components, 1, 0, -2, EnumRuneType.AIR);
        addRune(components, 1, 0, -1, EnumRuneType.AIR);
        addRune(components, 1, 0, 0, EnumRuneType.EARTH);
        addRune(components, 1, 0, 1, EnumRuneType.AIR);
        addRune(components, 1, 0, 2, EnumRuneType.AIR);
        addRune(components, 2, 0, -1, EnumRuneType.AIR);
        addRune(components, 2, 0, 1, EnumRuneType.AIR);
        addRune(components, -2, 1, -1, EnumRuneType.WATER);
        addRune(components, -2, 1, 1, EnumRuneType.WATER);
        addRune(components, -1, 1, -2, EnumRuneType.FIRE);
        addRune(components, -1, 1, 2, EnumRuneType.AIR);
        addRune(components, 1, 1, -2, EnumRuneType.FIRE);
        addRune(components, 1, 1, 2, EnumRuneType.AIR);
        addRune(components, 2, 1, -1, EnumRuneType.EARTH);
        addRune(components, 2, 1, 1, EnumRuneType.EARTH);
    }


    @Override
    public Ritual getNewCopy() {
        return new RitualSourceVitaeum();
    }

}
