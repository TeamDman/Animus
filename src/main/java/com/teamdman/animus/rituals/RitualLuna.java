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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

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
public class RitualLuna extends Ritual {
    private static final Logger LOGGER = LoggerFactory.getLogger(RitualLuna.class);

    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";

    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();

    public RitualLuna() {
        super(Constants.Rituals.LUNA, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.LUNA);

        addBlockRange(EFFECT_RANGE, RitualAreaDescriptors.largeCube65());
        addBlockRange(CHEST_RANGE, RitualAreaDescriptors.singleBlockAbove());

        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(mrs.getOwner());
        int currentEssence = network.getCurrentEssence();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        if (currentEssence < getRefreshCost()) {
            return;
        }

        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        BlockPos lightPos = findLightEmittingBlock(level, masterPos, effectRange);

        if (lightPos == null) {
            return;
        }

        BlockState state = level.getBlockState(lightPos);
        Block block = state.getBlock();

        ItemStack stack = block.getCloneItemStack(level, lightPos, state);

        boolean shouldRemoveBlock = true;
        if (!stack.isEmpty()) {
            if (chestTile != null) {
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
                if (handler != null) {
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack, true);
                    if (remainder.isEmpty()) {
                        ItemHandlerHelper.insertItem(handler, stack, false);
                    } else {
                        shouldRemoveBlock = false;
                    }
                } else {
                    shouldRemoveBlock = false;
                }
            } else {
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

        if (shouldRemoveBlock) {
            level.removeBlock(lightPos, false);

            SoulTicket ticket = SoulTicket.create(getRefreshCost());
            network.syphon(ticket);
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
            addCornerRunes(components, 2, layer, EnumRuneType.EARTH);
        }
    }

    private BlockPos findLightEmittingBlock(Level level, BlockPos masterPos, AreaDescriptor effectRange) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        int maxChecksPerTick = 4096; // Increased for faster operation
        int checksThisTick = 0;

        net.minecraft.world.phys.AABB aabb = effectRange.getAABB(masterPos);
        int horizontalRadius = (int) Math.max(
            Math.max(Math.abs(aabb.minX - masterPos.getX()), Math.abs(aabb.maxX - masterPos.getX())),
            Math.max(Math.abs(aabb.minZ - masterPos.getZ()), Math.abs(aabb.maxZ - masterPos.getZ()))
        );
        int verticalRadius = (int) Math.max(
            Math.abs(aabb.minY - masterPos.getY()),
            Math.abs(aabb.maxY - masterPos.getY())
        );

        BlockPos startPos = masterPos.below();
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

                        int manhattanDist = Math.abs(x) + Math.abs(z) + Math.abs(y);
                        if (manhattanDist != distance) {
                            continue;
                        }

                        BlockPos checkPos = startPos.offset(x, y, z);
                        checksThisTick++;

                        state.currentDistance = distance;
                        state.currentX = x;
                        state.currentZ = z;
                        state.currentY = y;

                        BlockState blockState = level.getBlockState(checkPos);
                        int lightEmission = blockState.getLightEmission();

                        if (lightEmission > 0) {
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

        if (state.currentDistance > horizontalRadius + verticalRadius) {
            searchStates.remove(masterPos);
        }

        return null;
    }

    private static class SearchState {
        int currentDistance = 0;
        int currentX = Integer.MIN_VALUE;
        int currentZ = Integer.MIN_VALUE;
        int currentY = Integer.MAX_VALUE;
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualLuna();
    }
}
