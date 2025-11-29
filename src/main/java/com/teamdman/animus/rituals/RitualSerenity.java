package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
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
 * Refresh Cost: Configurable (default: 50 LP)
 * Refresh Time: 20 ticks (1 second)
 * Range: Configurable (default: 48 blocks)
 */
@RitualRegister(Constants.Rituals.SERENITY)
public class RitualSerenity extends Ritual {
    // Track active ritual positions per level
    private static final Map<Level, Set<BlockPos>> activeRituals = new HashMap<>();

    public RitualSerenity() {
        super(
            Constants.Rituals.SERENITY,
            0,
            10000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SERENITY
        );
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel)) {
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

        // Add to active rituals
        addActiveRitual(level, masterPos);
    }

    /**
     * Add a ritual position to the active list
     */
    private static void addActiveRitual(Level level, BlockPos pos) {
        activeRituals.computeIfAbsent(level, k -> new HashSet<>()).add(pos.immutable());
    }

    /**
     * Remove a ritual position from the active list
     */
    private static void removeActiveRitual(Level level, BlockPos pos) {
        Set<BlockPos> rituals = activeRituals.get(level);
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
        Set<BlockPos> rituals = activeRituals.get(level);
        if (rituals == null || rituals.isEmpty()) {
            return false;
        }

        int radius = AnimusConfig.rituals.serenityRadius.get();
        int radiusSquared = radius * radius;

        for (BlockPos ritualPos : rituals) {
            if (spawnPos.distSqr(ritualPos) <= radiusSquared) {
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
