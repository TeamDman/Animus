package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Ritual of Luna - Harvests light-emitting blocks
 * Scans the effect range for blocks with light level > 0, harvests them, and stores in chest above ritual
 * Uses center-outward search algorithm to prioritize nearby positions
 * Activation Cost: 1000 LP
 * Refresh Cost: 1 LP
 * Refresh Time: 5 ticks
 */
@RitualRegister(Constants.Rituals.LUNA)
public class RitualLuna extends Ritual {
    private static final Logger LOGGER = LoggerFactory.getLogger(RitualLuna.class);

    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";

    // Track current search position for each ritual to resume searching where we left off
    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();

    public RitualLuna() {
        super(Constants.Rituals.LUNA, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.LUNA);

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        int currentEssence = network.getCurrentEssence();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        // Check if player has enough LP
        if (currentEssence < getRefreshCost()) {
            network.causeNausea();
            return;
        }

        // Get chest position
        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        // Find a light-emitting block using center-outward search
        BlockPos lightPos = findLightEmittingBlock(level, masterPos);

        if (lightPos == null) {
            return;
        }

        BlockState state = level.getBlockState(lightPos);
        Block block = state.getBlock();

        // Get the item stack for this block
        ItemStack stack = block.getCloneItemStack(level, lightPos, state);

        // Try to place item in chest if we have one
        boolean shouldRemoveBlock = true;
        if (!stack.isEmpty()) {
            // Try to insert into chest
            if (chestTile != null) {
                IItemHandler handler = chestTile.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
                if (handler != null) {
                    // Try to insert, if successful then we can proceed
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack, true);
                    if (remainder.isEmpty()) {
                        ItemHandlerHelper.insertItem(handler, stack, false);
                    } else {
                        // Chest is full, don't remove block
                        shouldRemoveBlock = false;
                    }
                } else {
                    // No handler, don't remove block
                    shouldRemoveBlock = false;
                }
            } else {
                // No chest, drop at master ritual stone
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    masterPos.getX() + 0.5,
                    masterPos.getY() + 1,
                    masterPos.getZ() + 0.5,
                    stack
                );
                level.addFreshEntity(itemEntity);
            }
        }

        // Remove block and consume LP (even if no valid item dropped)
        if (shouldRemoveBlock) {
            level.removeBlock(lightPos, false);

            // Consume LP
            SoulTicket ticket = new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_LUNA),
                getRefreshCost()
            );
            network.syphon(ticket, false);
        }
    }

    @Override
    public int getRefreshCost() {
        return 1;
    }

    @Override
    public int getRefreshTime() {
        return 5;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        for (int layer = 0; layer < 3; layer++) {
            addRune(components, 2, layer, 2, EnumRuneType.EARTH);
            addRune(components, -2, layer, 2, EnumRuneType.EARTH);
            addRune(components, 2, layer, -2, EnumRuneType.EARTH);
            addRune(components, -2, layer, -2, EnumRuneType.EARTH);
        }
    }

    /**
     * Find a light-emitting block using breadth-first search
     * Starts from 1 block below the master ritual stone and expands outward in all directions
     */
    private BlockPos findLightEmittingBlock(Level level, BlockPos masterPos) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        int maxChecksPerTick = 512; // Limit checks per tick to avoid lag
        int checksThisTick = 0;
        int horizontalRadius = AnimusConfig.rituals.lunaHorizontalRange.get();
        int configVerticalRange = AnimusConfig.rituals.lunaVerticalRange.get();

        // Start position is 1 block below the ritual stone
        BlockPos startPos = masterPos.below();

        // Calculate actual vertical radius
        // -1 means search from starting position to world bottom
        int verticalRadius;
        if (configVerticalRange == -1) {
            // Search from start position down to world minimum build height
            int minY = level.getMinBuildHeight();
            verticalRadius = startPos.getY() - minY;
        } else {
            verticalRadius = configVerticalRange;
        }

        // Breadth-first search: expand outward in "shells" of increasing distance
        // We use Manhattan distance for efficiency
        for (int distance = state.currentDistance; distance <= horizontalRadius + verticalRadius && checksThisTick < maxChecksPerTick; distance++) {
            // For each distance level, check all positions at that Manhattan distance from start
            for (int x = -horizontalRadius; x <= horizontalRadius && checksThisTick < maxChecksPerTick; x++) {
                // Skip if we're not resuming from this X position
                if (distance == state.currentDistance && x < state.currentX) continue;

                for (int z = -horizontalRadius; z <= horizontalRadius && checksThisTick < maxChecksPerTick; z++) {
                    // Skip if we're not resuming from this Z position
                    if (distance == state.currentDistance && x == state.currentX && z < state.currentZ) continue;

                    for (int y = 0; y >= -verticalRadius && checksThisTick < maxChecksPerTick; y--) {
                        // Skip if we're not resuming from this Y position
                        if (distance == state.currentDistance && x == state.currentX && z == state.currentZ && y > state.currentY) continue;

                        // Calculate Manhattan distance from start position
                        int manhattanDist = Math.abs(x) + Math.abs(z) + Math.abs(y);

                        // Only check positions at this exact distance
                        if (manhattanDist != distance) {
                            continue;
                        }

                        BlockPos checkPos = startPos.offset(x, y, z);
                        checksThisTick++;

                        // Update search state to resume from here next tick
                        state.currentDistance = distance;
                        state.currentX = x;
                        state.currentZ = z;
                        state.currentY = y;

                        // Check if this block emits light
                        BlockState blockState = level.getBlockState(checkPos);
                        int lightEmission = blockState.getLightEmission();

                        if (lightEmission > 0) {
                            // Advance to next position for next search
                            state.currentY--;
                            if (state.currentY < -verticalRadius) {
                                state.currentY = 0;
                                state.currentZ++;
                                if (state.currentZ > horizontalRadius) {
                                    state.currentZ = -horizontalRadius;
                                    state.currentX++;
                                    if (state.currentX > horizontalRadius) {
                                        state.currentDistance++;
                                        state.currentX = -horizontalRadius;
                                        state.currentZ = -horizontalRadius;
                                        state.currentY = 0;
                                    }
                                }
                            }
                            return checkPos;
                        }
                    }
                }
            }
        }

        // If we've searched the entire range, reset to start over next tick
        if (state.currentDistance > horizontalRadius + verticalRadius) {
            searchStates.remove(masterPos);
        }

        return null;
    }

    /**
     * Track the search state for each ritual to resume where it left off
     * Uses breadth-first search based on Manhattan distance from starting position
     * Starting position is 1 block below the ritual stone
     */
    private static class SearchState {
        int currentDistance = 0; // Current Manhattan distance being searched
        int currentX = -1; // X offset from start position (starts at -1 so first increment goes to 0)
        int currentZ = -1; // Z offset from start position
        int currentY = 0; // Y offset from start position (0 = start level, searches downward)
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualLuna();
    }
}
