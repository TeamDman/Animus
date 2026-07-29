package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.AnimusStartupConfig;
import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;
import net.minecraft.world.phys.AABB;

import com.breakinblocks.animusnv.util.RitualZoneTracker;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Serenity - Prevents mob spawning in a radius
 * Creates a peaceful zone where hostile mobs cannot spawn
 * Activation Cost: 10000 EV
 * Refresh Cost: Configurable (default: 50 EV)
 * Refresh Time: 20 ticks (1 second)
 * Range: Configurable (default: 48 blocks)
 */
public class RitualSerenity extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    private static final RitualZoneTracker ZONE_TRACKER = new RitualZoneTracker();

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

        if (level.isClientSide() || !(level instanceof ServerLevel)) {
            return;
        }

        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
        if (network == null) {
            removeActiveRitual(level, masterPos);
            return;
        }

        int currentEV = network.getCurrentEV();
        int refreshCost = getRefreshCost();

        if (currentEV < refreshCost) {
            removeActiveRitual(level, masterPos);
            return;
        }

        network.syphon(AnimaTicket.create(refreshCost));

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        AABB effectAABB = effectRange.getAABB(masterPos);
        addActiveRitual(level, masterPos, effectAABB);
    }

    private static void addActiveRitual(Level level, BlockPos pos, AABB aabb) {
        ZONE_TRACKER.add(level, pos, aabb);
    }

    private static void removeActiveRitual(Level level, BlockPos pos) {
        ZONE_TRACKER.remove(level, pos);
    }

    public static boolean isInSerenityZone(Level level, BlockPos spawnPos) {
        return ZONE_TRACKER.isInZone(level, spawnPos);
    }

    public void onRitualStopped(Level level, BlockPos masterPos) {
        removeActiveRitual(level, masterPos);
    }

    public static void cleanupLevel(Level level) {
        ZONE_TRACKER.cleanupLevel(level);
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.serenityEVPerTick.get();
    }

    @Override
    public int getRefreshTime() {
        return 20;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addParallelRunes(components, 2, 0, EnumRuneType.WATER);
        addCornerRunes(components, 1, 0, EnumRuneType.AIR);
        addCornerRunes(components, 2, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualSerenity();
    }
}
