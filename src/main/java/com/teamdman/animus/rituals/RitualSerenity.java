package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Serenity - Prevents mob spawning in a radius
 * Creates a peaceful zone where hostile mobs cannot spawn
 * Activation Cost: 10000 LP
 * Refresh Cost: Configurable (default: 1 LP), charged once per refresh
 * Refresh Time: Configurable (default: 20 ticks / 1 second), so the defaults work out to 1 LP per second
 * Range: Configurable (default: 48 blocks), modifiable via Ritual Tinkerer
 */
@RitualRegister(Constants.Rituals.SERENITY)
public class RitualSerenity extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    // Track active ritual positions per level with their AABB for spawn checking
    private static final Map<Level, Map<BlockPos, AABB>> activeRituals = new HashMap<>();

    public RitualSerenity() {
        super(
            Constants.Rituals.SERENITY,
            0,
            10000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SERENITY
        );

        int radius = AnimusConfig.rituals.serenityRadius.get();
        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(
            new BlockPos(-radius, -radius, -radius), radius * 2 + 1));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel)) {
            return;
        }
        // Check if ritual is enabled
        if (!AnimusConfig.rituals.serenityEnabled.get()) {
            removeActiveRitual(level, masterPos);
            return;
        }


        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
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
            network.causeNausea();
            return;
        }

        // Consume LP
        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_SERENITY),
            refreshCost
        ), false);

        // Get the effect range (respects Ritual Tinkerer modifications)
        AreaDescriptor effectRange = mrs.getBlockRange(EFFECT_RANGE);
        AABB aabb = effectRange.getAABB(masterPos);

        // Add to active rituals with the current AABB
        addActiveRitual(level, masterPos, aabb);
    }

    /**
     * Add a ritual position to the active list with its AABB
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
     * Uses stored AABB which respects Ritual Tinkerer modifications
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
    @Override
    public void stopRitual(IMasterRitualStone mrs, BreakType breakType) {
        removeActiveRitual(mrs.getWorldObj(), mrs.getMasterBlockPos());
        super.stopRitual(mrs, breakType);
    }

    /**
     * Clean up all rituals for a level (when unloading)
     */
    public static void cleanupLevel(Level level) {
        activeRituals.remove(level);
    }

    public static void tickActiveRituals(ServerLevel level) {
        Map<BlockPos, AABB> rituals = activeRituals.get(level);
        if (rituals == null) {
            return;
        }
        rituals.keySet().removeIf(pos -> !AnimusConfig.rituals.serenityEnabled.get()
            || !(level.getBlockEntity(pos) instanceof IMasterRitualStone mrs)
            || !mrs.isActive() || !(mrs.getCurrentRitual() instanceof RitualSerenity)
            || level.hasNeighborSignal(pos));
        if (rituals.isEmpty()) {
            activeRituals.remove(level);
        }
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.serenityLPPerTick.get();
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.serenityRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a peaceful pattern with water runes (representing calm)
        // and air runes (representing tranquility)

        // Inner circle with water runes
        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);

        // Diagonal positions with air runes
        addRune(components, -1, 0, -1, EnumRuneType.AIR);
        addRune(components, -1, 0, 1, EnumRuneType.AIR);
        addRune(components, 1, 0, -1, EnumRuneType.AIR);
        addRune(components, 1, 0, 1, EnumRuneType.AIR);

        // Outer corners with earth runes for grounding
        addRune(components, -2, 0, -2, EnumRuneType.EARTH);
        addRune(components, -2, 0, 2, EnumRuneType.EARTH);
        addRune(components, 2, 0, -2, EnumRuneType.EARTH);
        addRune(components, 2, 0, 2, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSerenity();
    }
}
