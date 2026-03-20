package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.AnimusStartupConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import com.breakinblocks.neovitae.common.item.ExperienceTomeItem;
import com.breakinblocks.neovitae.common.datacomponent.SoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.util.helper.SoulNetworkHelper;

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Endless Greed - Collects mob drops and XP into a container
 * When mobs die within range, their drops are transferred to a container on top of the ritual stone.
 * XP orbs are collected and stored in Tomes of Peritia found in the container.
 * If the container is full or missing, items are destroyed.
 *
 * Activation Cost: 5000 LP
 * Refresh Cost: 5 LP per cycle (configurable)
 * Refresh Time: 20 ticks (1 second)
 * Range: 15x15 horizontal, 5 high (configurable)
 */
public class RitualEndlessGreed extends Ritual {
    public static final String EFFECT_RANGE = "effect";
    public static final String CHEST_RANGE = "chest";

    private static final Map<Level, Map<BlockPos, AABB>> activeRituals = new HashMap<>();
    private static final Map<Level, Map<BlockPos, TomeCacheEntry>> tomeCache = new HashMap<>();

    private static class TomeCacheEntry {
        int tomeSlot;
        long lastUpdateTick;

        TomeCacheEntry(int slot, long tick) {
            this.tomeSlot = slot;
            this.lastUpdateTick = tick;
        }
    }

    public RitualEndlessGreed() {
        super(
            Constants.Rituals.ENDLESS_GREED,
            0,
            5000,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ENDLESS_GREED
        );

        int hRange = AnimusStartupConfig.ritualRanges.endlessGreedHorizontalRange.get();
        int vRange = AnimusStartupConfig.ritualRanges.endlessGreedVerticalRange.get();
        int hSize = hRange * 2 + 1;

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-hRange, 0, -hRange), hSize, vRange + 1, hSize));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1, 1, 1));

        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, hRange + 10, vRange + 10);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 0, 5, 5);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(mrs.getOwner());
        if (network == null) {
            removeActiveRitual(level, masterPos);
            return;
        }

        int currentEssence = network.getCurrentEssence();
        int refreshCost = getRefreshCost();

        if (currentEssence < refreshCost) {
            removeActiveRitual(level, masterPos);
            return;
        }

        network.syphon(SoulTicket.create(refreshCost));

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        AABB range = effectRange.getAABB(masterPos);

        addActiveRitual(level, masterPos, range);
        collectItemsInRange(serverLevel, masterPos, range, network);
        collectXPOrbsInRange(serverLevel, masterPos, range);
    }

    private void collectItemsInRange(ServerLevel level, BlockPos masterPos, AABB range, SoulNetwork network) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, range);

        if (items.isEmpty()) {
            return;
        }

        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos containerPos = chestRange.getContainedPositions(masterPos).iterator().next();
        IItemHandler itemHandler = getItemHandler(level, containerPos);

        int lpPerItem = AnimusConfig.rituals.endlessGreedLPPerItem.get();

        for (ItemEntity itemEntity : items) {
            if (itemEntity.tickCount < 5) {
                continue;
            }

            ItemStack stack = itemEntity.getItem().copy();

            if (itemHandler != null) {
                ItemStack remaining = insertItem(itemHandler, stack);

                if (remaining.isEmpty()) {
                    itemEntity.discard();

                    if (lpPerItem > 0) {
                        network.syphon(SoulTicket.create(lpPerItem * stack.getCount()));
                    }
                } else if (remaining.getCount() < stack.getCount()) {
                    itemEntity.setItem(remaining);

                    int inserted = stack.getCount() - remaining.getCount();
                    if (lpPerItem > 0 && inserted > 0) {
                        network.syphon(SoulTicket.create(lpPerItem * inserted));
                    }
                }
            } else {
                itemEntity.discard();
            }
        }
    }

    private void collectXPOrbsInRange(ServerLevel level, BlockPos masterPos, AABB range) {
        List<ExperienceOrb> xpOrbs = level.getEntitiesOfClass(ExperienceOrb.class, range);

        if (xpOrbs.isEmpty()) {
            return;
        }

        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos containerPos = chestRange.getContainedPositions(masterPos).iterator().next();
        IItemHandler itemHandler = getItemHandler(level, containerPos);

        int tomeSlot = getCachedTomeSlot(level, masterPos, itemHandler);

        if (tomeSlot >= 0 && itemHandler != null && tomeSlot < itemHandler.getSlots()) {
            ItemStack stack = itemHandler.getStackInSlot(tomeSlot);
            if (stack.getItem() instanceof ExperienceTomeItem) {
                int totalXP = 0;
                for (ExperienceOrb orb : xpOrbs) {
                    totalXP += orb.getValue();
                }
                ExperienceTomeItem.addXpToTome(stack, totalXP);
            }
        }
        // Remove all XP orbs (discarded if no tome found)
        for (ExperienceOrb orb : xpOrbs) {
            orb.discard();
        }
    }

    private int getCachedTomeSlot(ServerLevel level, BlockPos masterPos, IItemHandler itemHandler) {
        if (itemHandler == null) {
            return -1;
        }

        Map<BlockPos, TomeCacheEntry> levelCache = tomeCache.get(level);
        TomeCacheEntry entry = levelCache != null ? levelCache.get(masterPos) : null;

        long currentTick = level.getGameTime();

        // Invalidate every 100 ticks to catch inventory changes
        if (entry != null && (currentTick - entry.lastUpdateTick) < 100) {
            return entry.tomeSlot;
        }

        int foundSlot = -1;
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ExperienceTomeItem) {
                foundSlot = slot;
                break;
            }
        }

        if (levelCache == null) {
            levelCache = new HashMap<>();
            tomeCache.put(level, levelCache);
        }
        levelCache.put(masterPos.immutable(), new TomeCacheEntry(foundSlot, currentTick));

        return foundSlot;
    }

    public static void invalidateTomeCache(Level level, BlockPos masterPos) {
        Map<BlockPos, TomeCacheEntry> levelCache = tomeCache.get(level);
        if (levelCache != null) {
            levelCache.remove(masterPos);
        }
    }

    private IItemHandler getItemHandler(Level level, BlockPos pos) {
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
    }

    private ItemStack insertItem(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }

        return remaining;
    }

    public static boolean handleMobDrops(Level level, BlockPos deathPos, Collection<ItemEntity> drops) {
        Map<BlockPos, AABB> rituals = activeRituals.get(level);
        if (rituals == null || rituals.isEmpty()) {
            return false;
        }

        for (Map.Entry<BlockPos, AABB> entry : rituals.entrySet()) {
            if (entry.getValue().contains(deathPos.getX() + 0.5, deathPos.getY() + 0.5, deathPos.getZ() + 0.5)) {
                BlockPos masterPos = entry.getKey();
                BlockPos containerPos = masterPos.above();

                IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, containerPos, null);

                if (itemHandler == null) {
                    return true;
                }

                for (ItemEntity itemEntity : drops) {
                    ItemStack stack = itemEntity.getItem();
                    if (stack.isEmpty()) continue;

                    ItemStack toInsert = stack.copy();
                    for (int slot = 0; slot < itemHandler.getSlots() && !toInsert.isEmpty(); slot++) {
                        toInsert = itemHandler.insertItem(slot, toInsert, false);
                    }
                }

                return true;
            }
        }

        return false;
    }

    private static void addActiveRitual(Level level, BlockPos pos, AABB range) {
        activeRituals.computeIfAbsent(level, k -> new HashMap<>()).put(pos.immutable(), range);
    }

    private static void removeActiveRitual(Level level, BlockPos pos) {
        Map<BlockPos, AABB> rituals = activeRituals.get(level);
        if (rituals != null) {
            rituals.remove(pos);
            if (rituals.isEmpty()) {
                activeRituals.remove(level);
            }
        }

        Map<BlockPos, TomeCacheEntry> levelCache = tomeCache.get(level);
        if (levelCache != null) {
            levelCache.remove(pos);
        }
    }

    public static boolean isInGreedZone(Level level, BlockPos pos) {
        Map<BlockPos, AABB> rituals = activeRituals.get(level);
        if (rituals == null || rituals.isEmpty()) {
            return false;
        }

        for (AABB range : rituals.values()) {
            if (range.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                return true;
            }
        }

        return false;
    }

    public void onRitualStopped(Level level, BlockPos masterPos) {
        removeActiveRitual(level, masterPos);
    }

    public static void cleanupLevel(Level level) {
        activeRituals.remove(level);
        tomeCache.remove(level);
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.endlessGreedRefreshCost.get();
    }

    @Override
    public int getRefreshTime() {
        return 20;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, 0, 0, -1, EnumRuneType.EARTH);
        addRune(components, 0, 0, 1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 0, EnumRuneType.EARTH);
        addRune(components, 1, 0, 0, EnumRuneType.EARTH);

        addRune(components, -1, 0, -1, EnumRuneType.FIRE);
        addRune(components, -1, 0, 1, EnumRuneType.FIRE);
        addRune(components, 1, 0, -1, EnumRuneType.FIRE);
        addRune(components, 1, 0, 1, EnumRuneType.FIRE);

        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);

        addRune(components, -2, 0, -2, EnumRuneType.AIR);
        addRune(components, -2, 0, 2, EnumRuneType.AIR);
        addRune(components, 2, 0, -2, EnumRuneType.AIR);
        addRune(components, 2, 0, 2, EnumRuneType.AIR);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualEndlessGreed();
    }
}
