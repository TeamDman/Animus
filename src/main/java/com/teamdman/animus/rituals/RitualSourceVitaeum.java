package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.CompatHandler;
import com.teamdman.animus.compat.arsnouveau.SourceJarHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.common.tile.TileAltar;

import java.util.function.Consumer;

/**
 * Ritual of Source Vitaeum - Converts Ars Nouveau Source into Blood Magic LP
 *
 * This ritual creates a bridge between Ars Nouveau's Source system and Blood Magic's LP system,
 * allowing hybrid players to convert excess Source into LP at a configurable exchange rate.
 *
 * Features:
 * - Requires a Source Jar directly above the Master Ritual Stone (at y+1)
 * - Searches for Blood Altars within configurable radius (default 8 blocks)
 * - Drains Source from the jar above the ritual
 * - Base conversion: 10 Source to 1 LP (configurable)
 * - Penalty system: Each nearby Master Ritual Stone doubles the conversion cost
 * - Respects altar speed runes, transfer limits, and dislocation runes
 * - Optional integration: Only active when Ars Nouveau is installed
 *
 * Activation Cost: 10000 LP
 * Refresh Cost: 0 LP (conversion happens via Source drain)
 * Refresh Time: Based on altar speed (default 40 ticks / 2 seconds)
 */
@RitualRegister(Constants.Rituals.SOURCE_VITAEUM)
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

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Check if Ars Nouveau is loaded
        if (!CompatHandler.isArsNouveauLoaded()) {
            // Ritual is inactive without Ars Nouveau
            return;
        }

        // Find nearby Blood Altar
        int altarSearchRadius = AnimusConfig.rituals.sourceVitaeumAltarRange.get();
        TileAltar altar = findNearbyAltar(serverLevel, masterPos, altarSearchRadius);

        if (altar == null) {
            // No altar found, ritual cannot function
            return;
        }

        // Count nearby Master Ritual Stones for penalty calculation
        int penaltyRadius = AnimusConfig.rituals.sourceVitaeumPenaltyRadius.get();
        int nearbyRituals = countNearbyMasterRitualStones(serverLevel, altar.getBlockPos(), penaltyRadius);

        // Calculate conversion rate with penalty
        // Base rate is 10:1, each nearby ritual doubles the cost (20:1, 40:1, etc.)
        int baseConversion = AnimusConfig.rituals.sourceVitaeumBaseConversion.get();
        int conversionRate = baseConversion * (int)Math.pow(2, nearbyRituals);

        // Amount of Source to attempt to drain per cycle
        int sourcePerCycle = AnimusConfig.rituals.sourceVitaeumSourcePerCycle.get();

        // Find Source Jars and drain them
        int sourceDrained = SourceJarHelper.drainSourceFromJarAbove(serverLevel, masterPos, sourcePerCycle);

        if (sourceDrained <= 0) {
            // No Source available to convert
            return;
        }

        // Calculate LP to add based on Source drained and conversion rate
        int lpToAdd = sourceDrained / conversionRate;

        if (lpToAdd <= 0) {
            // Not enough Source for even 1 LP
            return;
        }

        // Check altar capacity (don't exceed max capacity)
        int currentBlood = altar.getCurrentBlood();
        int maxBlood = altar.getCapacity();
        int availableSpace = maxBlood - currentBlood;

        if (availableSpace <= 0) {
            // Altar is full
            return;
        }

        // Don't overflow the altar
        lpToAdd = Math.min(lpToAdd, availableSpace);

        if (lpToAdd <= 0) {
            return;
        }

        // Add LP to altar using the standard method
        // The second parameter (true) makes it respect altar speed and dislocation runes
        altar.sacrificialDaggerCall(lpToAdd, true);
    }

    /**
     * Find a Blood Altar within the specified radius
     */
    private TileAltar findNearbyAltar(ServerLevel level, BlockPos center, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
            center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius)
        )) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TileAltar altar) {
                return altar;
            }
        }
        return null;
    }

    /**
     * Count Master Ritual Stones within radius of the altar (excluding this ritual)
     */
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
        // Subtract 1 because we count ourselves
        return Math.max(0, count - 1);
    }

    @Override
    public int getRefreshCost() {
        return 0; // No LP cost, conversion happens via Source drain
    }

    @Override
    public int getRefreshTime() {
        // Base time is 40 ticks (2 seconds)
        // This will be modified by altar speed runes automatically
        return 40;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Small footprint ritual - 3x3 pattern with Dusk runes (representing transmutation)
        // and Earth runes (representing grounding/stability)

        // Inner cross with Dusk runes (transmutation)
        addRune(components, 0, 0, -1, EnumRuneType.DUSK);
        addRune(components, 0, 0, 1, EnumRuneType.DUSK);
        addRune(components, -1, 0, 0, EnumRuneType.DUSK);
        addRune(components, 1, 0, 0, EnumRuneType.DUSK);

        // Corners with Earth runes (stability)
        addRune(components, -1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.EARTH);
        addRune(components, 1, 0, -1, EnumRuneType.EARTH);
        addRune(components, 1, 0, 1, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSourceVitaeum();
    }

}
