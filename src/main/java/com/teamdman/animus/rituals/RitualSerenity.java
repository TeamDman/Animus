package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.AnimusStartupConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.common.datacomponent.SoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;
import com.breakinblocks.neovitae.util.helper.SoulNetworkHelper;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Serenity - Prevents mob spawning in a radius
 * Creates a peaceful zone where hostile mobs cannot spawn
 * Activation Cost: 10000 LP
 * Refresh Cost: Configurable (default: 50 LP)
 * Refresh Time: 20 ticks (1 second)
 * Range: Configurable (default: 48 blocks)
 */
public class RitualSerenity extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    // Track active ritual positions and their AABBs per level
    private static final Map<Level, Map<BlockPos, AABB>> activeRituals = new HashMap<>();

    public RitualSerenity() {
        super(
            Constants.Rituals.SERENITY,
            0,
            10000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SERENITY
        );

        int radius = AnimusStartupConfig.ritualRanges.serenityRadius.get();
        int size = radius * 2 + 1;

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-radius, -radius, -radius), size, size, size));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, radius + 32, radius + 32);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return;
        }

        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            // Remove from active list if network is gone
            removeActiveRitual(level, masterPos);
            return;
        }

        int currentEssence = network.getCurrentEssence();
        int refreshCost = getRefreshCost();

        // Check if we have enough LP
        if (currentEssence < refreshCost) {
            // Not enough LP - remove from active list
            removeActiveRitual(level, masterPos);
            // Note: causeNausea removed in BM 4.0
            return;
        }

        // Consume LP
        network.syphon(SoulTicket.create(refreshCost));

        // Add to active rituals with the current effect AABB
        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        AABB effectAABB = effectRange.getAABB(masterPos);
        addActiveRitual(level, masterPos, effectAABB);
    }

    /**
     * Add a ritual position and its AABB to the active list
     */
    private static void addActiveRitual(Level level, BlockPos pos, AABB aabb) {
        activeRituals.computeIfAbsent(level, k -> new HashMap<>()).put(pos.immutable(), aabb);
    }

    /**
     * Remove a ritual position from the active list
     */
    private static void removeActiveRitual(Level level, BlockPos pos) {
        Map<BlockPos, AABB> rituals = activeRituals.get(level);
        if (rituals != null) {
            rituals.remove(pos);
            if (rituals.isEmpty()) {
                activeRituals.remove(level);
            }
        }
    }

    /**
     * Check if a position is within range of any active Serenity ritual
     */
    public static boolean isInSerenityZone(Level level, BlockPos spawnPos) {
        Map<BlockPos, AABB> rituals = activeRituals.get(level);
        if (rituals == null || rituals.isEmpty()) {
            return false;
        }

        for (AABB aabb : rituals.values()) {
            if (aabb.contains(spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Clean up ritual when it stops
     */
    public void onRitualStopped(Level level, BlockPos masterPos) {
        removeActiveRitual(level, masterPos);
    }

    /**
     * Clean up all rituals for a level (when unloading)
     */
    public static void cleanupLevel(Level level) {
        activeRituals.remove(level);
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.serenityLPPerTick.get();
    }

    @Override
    public int getRefreshTime() {
        return 20; // 1 second
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a peaceful pattern with water runes (representing calm)
        // and air runes (representing tranquility)

        // Inner circle with water runes (cardinal directions)
        addParallelRunes(components, 2, 0, EnumRuneType.WATER);

        // Diagonal positions with air runes
        addCornerRunes(components, 1, 0, EnumRuneType.AIR);

        // Outer corners with earth runes for grounding
        addCornerRunes(components, 2, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSerenity();
    }
}
