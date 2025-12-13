package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
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
import wayoftime.bloodmagic.common.item.ExperienceTomeItem;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

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
    // Track active ritual positions and their AABBs for the event handler
    private static final Map<Level, Map<BlockPos, AABB>> activeRituals = new HashMap<>();

    // Cache for Tome of Peritia slot per ritual position
    // Outer map: Level -> (RitualPos -> TomeCacheEntry)
    private static final Map<Level, Map<BlockPos, TomeCacheEntry>> tomeCache = new HashMap<>();

    /**
     * Cache entry for tome slot in a container
     */
    private static class TomeCacheEntry {
        int tomeSlot; // Cached slot index containing tome (-1 if none found)
        long lastUpdateTick; // Tick when cache was last updated

        TomeCacheEntry(int slot, long tick) {
            this.tomeSlot = slot;
            this.lastUpdateTick = tick;
        }
    }

    public RitualEndlessGreed() {
        super(
            Constants.Rituals.ENDLESS_GREED,
            0,
            5000,  // Activation cost
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ENDLESS_GREED
        );
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

        // Check if we have enough LP
        if (currentEssence < refreshCost) {
            removeActiveRitual(level, masterPos);
            // Note: causeNausea removed in BM 4.0
            return;
        }

        // Consume LP
        network.syphon(SoulTicket.create(refreshCost));

        // Calculate range
        int hRange = AnimusConfig.rituals.endlessGreedRange.get();
        int vRange = AnimusConfig.rituals.endlessGreedVerticalRange.get();

        AABB range = new AABB(
            masterPos.getX() - hRange,
            masterPos.getY(),
            masterPos.getZ() - hRange,
            masterPos.getX() + hRange + 1,
            masterPos.getY() + vRange + 1,
            masterPos.getZ() + hRange + 1
        );

        // Register this ritual as active
        addActiveRitual(level, masterPos, range);

        // Collect any existing item entities in range (for items that might have been missed)
        collectItemsInRange(serverLevel, masterPos, range, network);

        // Collect XP orbs in range (runs every 20 ticks with performRitual)
        collectXPOrbsInRange(serverLevel, masterPos, range);
    }

    /**
     * Collect any item entities currently in range
     */
    private void collectItemsInRange(ServerLevel level, BlockPos masterPos, AABB range, SoulNetwork network) {
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, range);

        if (items.isEmpty()) {
            return;
        }

        BlockPos containerPos = masterPos.above();
        IItemHandler itemHandler = getItemHandler(level, containerPos);

        int lpPerItem = AnimusConfig.rituals.endlessGreedLPPerItem.get();

        for (ItemEntity itemEntity : items) {
            // Skip items that were just spawned (give them a moment to exist)
            if (itemEntity.tickCount < 5) {
                continue;
            }

            ItemStack stack = itemEntity.getItem().copy();

            if (itemHandler != null) {
                // Try to insert into container
                ItemStack remaining = insertItem(itemHandler, stack);

                if (remaining.isEmpty()) {
                    // All items inserted
                    itemEntity.discard();

                    // Consume LP per item
                    if (lpPerItem > 0) {
                        network.syphon(SoulTicket.create(lpPerItem * stack.getCount()));
                    }
                } else if (remaining.getCount() < stack.getCount()) {
                    // Partial insert - update entity with remaining
                    itemEntity.setItem(remaining);

                    // Consume LP for items that were inserted
                    int inserted = stack.getCount() - remaining.getCount();
                    if (lpPerItem > 0 && inserted > 0) {
                        network.syphon(SoulTicket.create(lpPerItem * inserted));
                    }
                }
                // If container is full (remaining == stack), leave item on ground
            } else {
                // No container - destroy items
                itemEntity.discard();
            }
        }
    }

    /**
     * Collect XP orbs in range and store in Tome of Peritia
     */
    private void collectXPOrbsInRange(ServerLevel level, BlockPos masterPos, AABB range) {
        List<ExperienceOrb> xpOrbs = level.getEntitiesOfClass(ExperienceOrb.class, range);

        if (xpOrbs.isEmpty()) {
            return;
        }

        BlockPos containerPos = masterPos.above();
        IItemHandler itemHandler = getItemHandler(level, containerPos);

        // Get cached tome slot or rebuild cache
        int tomeSlot = getCachedTomeSlot(level, masterPos, itemHandler);

        // Add XP to tome if found
        if (tomeSlot >= 0 && itemHandler != null && tomeSlot < itemHandler.getSlots()) {
            ItemStack stack = itemHandler.getStackInSlot(tomeSlot);
            if (stack.getItem() instanceof ExperienceTomeItem) {
                // Calculate total XP and add to tome (unlimited capacity)
                int totalXP = 0;
                for (ExperienceOrb orb : xpOrbs) {
                    totalXP += orb.getValue();
                }
                ExperienceTomeItem.addExperience(stack, totalXP);
            }
        }
        // If no tome found, XP is simply discarded

        // Remove all XP orbs
        for (ExperienceOrb orb : xpOrbs) {
            orb.discard();
        }
    }

    /**
     * Get cached tome slot, rebuilding cache if necessary
     * @return slot index of first tome found, or -1 if none
     */
    private int getCachedTomeSlot(ServerLevel level, BlockPos masterPos, IItemHandler itemHandler) {
        if (itemHandler == null) {
            return -1;
        }

        Map<BlockPos, TomeCacheEntry> levelCache = tomeCache.get(level);
        TomeCacheEntry entry = levelCache != null ? levelCache.get(masterPos) : null;

        long currentTick = level.getGameTime();

        // Check if cache is valid (invalidate every 100 ticks or ~5 seconds to catch inventory changes)
        if (entry != null && (currentTick - entry.lastUpdateTick) < 100) {
            return entry.tomeSlot;
        }

        // Rebuild cache - find first tome
        int foundSlot = -1;
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ExperienceTomeItem) {
                foundSlot = slot;
                break;
            }
        }

        // Store in cache
        if (levelCache == null) {
            levelCache = new HashMap<>();
            tomeCache.put(level, levelCache);
        }
        levelCache.put(masterPos.immutable(), new TomeCacheEntry(foundSlot, currentTick));

        return foundSlot;
    }

    /**
     * Invalidate tome cache for a specific ritual position
     */
    public static void invalidateTomeCache(Level level, BlockPos masterPos) {
        Map<BlockPos, TomeCacheEntry> levelCache = tomeCache.get(level);
        if (levelCache != null) {
            levelCache.remove(masterPos);
        }
    }

    /**
     * Get the item handler for a container at the given position
     */
    private IItemHandler getItemHandler(Level level, BlockPos pos) {
        // NeoForge 1.21 capability API
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
    }

    /**
     * Try to insert an item stack into an item handler
     * @return The remaining items that couldn't be inserted
     */
    private ItemStack insertItem(IItemHandler handler, ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, false);
        }

        return remaining;
    }

    /**
     * Called by LivingDropsEvent to handle mob drops
     * Returns true if the drops were handled (either collected or destroyed)
     */
    public static boolean handleMobDrops(Level level, BlockPos deathPos, Collection<ItemEntity> drops) {
        Map<BlockPos, AABB> rituals = activeRituals.get(level);
        if (rituals == null || rituals.isEmpty()) {
            return false;
        }

        // Find a ritual that contains this death position
        for (Map.Entry<BlockPos, AABB> entry : rituals.entrySet()) {
            if (entry.getValue().contains(deathPos.getX() + 0.5, deathPos.getY() + 0.5, deathPos.getZ() + 0.5)) {
                BlockPos masterPos = entry.getKey();
                BlockPos containerPos = masterPos.above();

                // NeoForge 1.21 capability API
                IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, containerPos, null);

                if (itemHandler == null) {
                    // No container found - items will be destroyed
                    return true;
                }

                // Process each drop
                for (ItemEntity itemEntity : drops) {
                    ItemStack stack = itemEntity.getItem();
                    if (stack.isEmpty()) continue;

                    // Try to insert into container
                    ItemStack toInsert = stack.copy();
                    for (int slot = 0; slot < itemHandler.getSlots() && !toInsert.isEmpty(); slot++) {
                        toInsert = itemHandler.insertItem(slot, toInsert, false);
                    }
                    // If toInsert is not empty, container is full - remaining items are destroyed
                }

                // Return true to indicate drops were handled
                return true;
            }
        }

        return false;
    }

    /**
     * Add a ritual position to the active list
     */
    private static void addActiveRitual(Level level, BlockPos pos, AABB range) {
        activeRituals.computeIfAbsent(level, k -> new HashMap<>()).put(pos.immutable(), range);
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

        // Also clear tome cache for this ritual
        Map<BlockPos, TomeCacheEntry> levelCache = tomeCache.get(level);
        if (levelCache != null) {
            levelCache.remove(pos);
        }
    }

    /**
     * Check if a position is within range of any active Endless Greed ritual
     */
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
        tomeCache.remove(level);
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.endlessGreedRefreshCost.get();
    }

    @Override
    public int getRefreshTime() {
        return 20; // 1 second
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Flat design using only basic runes (no dusk runes)
        // A greed-themed pattern with earth (wealth) and fire (ambition)

        // Inner cross with earth runes (representing wealth/treasure)
        addRune(components, 0, 0, -1, EnumRuneType.EARTH);
        addRune(components, 0, 0, 1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 0, EnumRuneType.EARTH);
        addRune(components, 1, 0, 0, EnumRuneType.EARTH);

        // Diagonal positions with fire runes (ambition/desire)
        addRune(components, -1, 0, -1, EnumRuneType.FIRE);
        addRune(components, -1, 0, 1, EnumRuneType.FIRE);
        addRune(components, 1, 0, -1, EnumRuneType.FIRE);
        addRune(components, 1, 0, 1, EnumRuneType.FIRE);

        // Outer cardinal positions with water runes (flow of items)
        addRune(components, 0, 0, -2, EnumRuneType.WATER);
        addRune(components, 0, 0, 2, EnumRuneType.WATER);
        addRune(components, -2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);

        // Outer corners with air runes (reaching/collecting)
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
