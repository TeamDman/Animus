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
     * Find a light-emitting block using center-outward search
     * Starts from the master ritual stone and expands outward in square rings
     */
    private BlockPos findLightEmittingBlock(Level level, BlockPos masterPos) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        int maxChecksPerTick = 64; // Limit checks per tick to avoid lag
        int checksThisTick = 0;
        int horizontalRadius = AnimusConfig.rituals.lunaHorizontalRange.get();
        int configVerticalRange = AnimusConfig.rituals.lunaVerticalRange.get();

        // Calculate actual vertical radius
        // -1 means search from ritual stone to world bottom
        int verticalRadius;
        if (configVerticalRange == -1) {
            // Search from ritual stone down to world minimum build height
            int minY = level.getMinBuildHeight();
            verticalRadius = masterPos.getY() - minY;
        } else {
            verticalRadius = configVerticalRange;
        }

        // Search by expanding square rings from center outward
        for (int radius = state.currentRadius; radius <= horizontalRadius && checksThisTick < maxChecksPerTick; radius++) {
            // Iterate through all positions in this radius ring
            for (int x = -radius; x <= radius && checksThisTick < maxChecksPerTick; x++) {
                // Skip if we're not resuming from this X position
                if (radius == state.currentRadius && x < state.currentX) continue;

                for (int z = -radius; z <= radius && checksThisTick < maxChecksPerTick; z++) {
                    // Skip if we're not resuming from this Z position
                    if (radius == state.currentRadius && x == state.currentX && z < state.currentZ) continue;

                    // Only check positions on the perimeter of this radius ring
                    // (except for radius 0, which is just the center point)
                    if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    // Search vertically downward from this column (from ritual stone to bottom)
                    int startY = (radius == state.currentRadius && x == state.currentX && z == state.currentZ) ? state.currentY : 0;
                    for (int y = startY; y >= -verticalRadius && checksThisTick < maxChecksPerTick; y--) {
                        BlockPos checkPos = masterPos.offset(x, y, z);
                        checksThisTick++;

                        // Update search state to resume from here next tick
                        state.currentRadius = radius;
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
                                if (state.currentZ > radius) {
                                    state.currentZ = -radius;
                                    state.currentX++;
                                    if (state.currentX > radius) {
                                        state.currentX = -radius;
                                        state.currentZ = -radius;
                                        state.currentY = 0;
                                        state.currentRadius++;
                                    }
                                }
                            }
                            return checkPos;
                        }
                    }
                    // Column complete, reset Y for next column
                    state.currentY = 0;
                }
                // Row complete, reset Z for next row
                state.currentZ = -radius;
            }
        }

        // If we've searched the entire range, reset to start over next tick
        if (state.currentRadius > horizontalRadius) {
            searchStates.remove(masterPos);
        }

        return null;
    }

    /**
     * Track the search state for each ritual to resume where it left off
     * Searches from center outward in expanding square rings
     * Searches downward from ritual stone (Y=0) to Y=-verticalRadius
     */
    private static class SearchState {
        int currentRadius = 0; // Start from center (at master ritual stone)
        int currentX = 0; // X position within current radius ring
        int currentZ = 0; // Z position within current radius ring
        int currentY = 0; // Vertical position (0 = ritual stone level, searches downward)
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualLuna();
    }
}
