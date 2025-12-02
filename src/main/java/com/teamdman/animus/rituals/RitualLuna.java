package com.teamdman.animus.rituals;

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

        // If we got a valid stack
        if (!stack.isEmpty()) {
            // Try to insert into chest
            if (chestTile != null) {
                IItemHandler handler = chestTile.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
                if (handler != null) {
                    // Try to insert, if successful then remove block
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack, true);
                    if (remainder.isEmpty()) {
                        ItemHandlerHelper.insertItem(handler, stack, false);
                        level.removeBlock(lightPos, false);

                        // Consume LP
                        SoulTicket ticket = new SoulTicket(
                            Component.translatable(Constants.Localizations.Text.TICKET_LUNA),
                            getRefreshCost()
                        );
                        network.syphon(ticket, false);
                    }
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
                level.removeBlock(lightPos, false);

                // Consume LP
                SoulTicket ticket = new SoulTicket(
                    Component.translatable(Constants.Localizations.Text.TICKET_LUNA),
                    getRefreshCost()
                );
                network.syphon(ticket, false);
            }
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
        int horizontalRadius = 32;
        int verticalRadius = 32;

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

                    // Search vertically around this column
                    int startY = (radius == state.currentRadius && x == state.currentX && z == state.currentZ) ? state.currentY : -verticalRadius;
                    for (int y = startY; y <= verticalRadius && checksThisTick < maxChecksPerTick; y++) {
                        BlockPos checkPos = masterPos.offset(x, y, z);
                        checksThisTick++;

                        // Update search state to resume from here next tick
                        state.currentRadius = radius;
                        state.currentX = x;
                        state.currentZ = z;
                        state.currentY = y;

                        // Check if this block emits light
                        BlockState blockState = level.getBlockState(checkPos);
                        if (blockState.getLightEmission() > 0) {
                            // Advance to next position for next search
                            state.currentY++;
                            if (state.currentY > verticalRadius) {
                                state.currentY = -verticalRadius;
                                state.currentZ++;
                                if (state.currentZ > radius) {
                                    state.currentZ = -radius;
                                    state.currentX++;
                                    if (state.currentX > radius) {
                                        state.currentX = -radius;
                                        state.currentZ = -radius;
                                        state.currentY = -verticalRadius;
                                        state.currentRadius++;
                                    }
                                }
                            }
                            return checkPos;
                        }
                    }
                    // Column complete, reset Y for next column
                    state.currentY = -verticalRadius;
                }
                // Row complete, reset Z for next row
                state.currentZ = -radius;
            }
            // Ring complete, move to next radius
            state.currentX = -radius - 1;
            state.currentZ = -radius - 1;
            state.currentY = -verticalRadius;
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
     */
    private static class SearchState {
        int currentRadius = 0; // Start from center (at master ritual stone)
        int currentX = 0; // X position within current radius ring
        int currentZ = 0; // Z position within current radius ring
        int currentY = -32; // Vertical position (-32 to 32, starts at bottom)
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualLuna();
    }
}
