package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Ritual of Sol - Places light sources in dark areas
 * Takes blocks from chest above ritual and places them in dark spots (light level < 8)
 * with solid ground below
 * Uses center-outward search algorithm to prioritize nearby positions
 * Supports Blood Magic's Sigil of Blood Light for placing blood lights without consuming the sigil
 * Activation Cost: 1000 LP
 * Refresh Cost: 1 LP (regular blocks) or 1 LP (blood light)
 * Refresh Time: 5 ticks
 */
@RitualRegister(Constants.Rituals.SOL)
public class RitualSol extends Ritual {
    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";
    private static final ResourceLocation BLOOD_LIGHT_SIGIL = ResourceLocation.fromNamespaceAndPath("bloodmagic", "bloodlightsigil");
    private static final ResourceLocation BLOOD_LIGHT_BLOCK = ResourceLocation.fromNamespaceAndPath("bloodmagic", "bloodlight");

    // Track current search position for each ritual to resume searching where we left off
    private static final Map<BlockPos, SearchState> searchStates = new HashMap<>();

    public RitualSol() {
        super(Constants.Rituals.SOL, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.SOL);

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        // Get chest
        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        if (chestTile == null) {
            return;
        }

        // Get item handler from chest
        IItemHandler handler = chestTile.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler == null) {
            return;
        }

        // Find a non-empty slot with a valid block item
        Optional<Integer> slotOpt = IntStream.range(0, handler.getSlots())
            .filter(i -> !handler.getStackInSlot(i).isEmpty())
            .filter(i -> isOkayToUse(handler.getStackInSlot(i)))
            .boxed()
            .findAny();

        if (!slotOpt.isPresent()) {
            return;
        }

        int slot = slotOpt.get();
        ItemStack stack = handler.getStackInSlot(slot);

        // Find a dark spot to place the block using center-outward search
        BlockPos placePos = findDarkSpot(level, masterPos);

        if (placePos == null) {
            return;
        }

        // Get the block state to place
        BlockState stateToPlace = getStateToUse(stack);

        // Place the block
        level.setBlock(placePos, stateToPlace, 3);

        // Check if this is a blood light sigil (doesn't consume)
        boolean isBloodLightSigil = isBloodLightSigil(stack);

        // Extract item from chest (unless it's the blood light sigil)
        if (!isBloodLightSigil && stack.getItem() instanceof BlockItem) {
            handler.extractItem(slot, 1, false);
        }

        // Consume LP
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_SOL),
            getRefreshCost()
        );
        network.syphon(ticket, false);
    }

    /**
     * Check if an item is the Blood Light sigil
     */
    private boolean isBloodLightSigil(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return BLOOD_LIGHT_SIGIL.equals(itemId);
    }

    /**
     * Check if an item can be used by this ritual
     */
    private boolean isOkayToUse(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        // Check for Blood Magic's Sigil of Blood Light
        if (isBloodLightSigil(stack)) {
            return true;
        }

        // Check if it's a block item
        return stack.getItem() instanceof BlockItem;
    }

    /**
     * Get the block state to place
     */
    private BlockState getStateToUse(ItemStack stack) {
        // Check for Blood Magic's Sigil of Blood Light
        if (isBloodLightSigil(stack)) {
            Block bloodLight = ForgeRegistries.BLOCKS.getValue(BLOOD_LIGHT_BLOCK);
            if (bloodLight != null && bloodLight != Blocks.AIR) {
                return bloodLight.defaultBlockState();
            }
        }

        // Get the block from the item
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }

        return Blocks.AIR.defaultBlockState();
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
            addRune(components, 2, layer, 2, EnumRuneType.AIR);
            addRune(components, -2, layer, 2, EnumRuneType.AIR);
            addRune(components, 2, layer, -2, EnumRuneType.AIR);
            addRune(components, -2, layer, -2, EnumRuneType.AIR);
        }
    }

    /**
     * Find a dark spot using center-outward search
     * Starts from the master ritual stone and expands outward in square rings
     */
    private BlockPos findDarkSpot(Level level, BlockPos masterPos) {
        SearchState state = searchStates.computeIfAbsent(masterPos.immutable(), k -> new SearchState());

        int maxChecksPerTick = 64; // Limit checks per tick to avoid lag
        int checksThisTick = 0;
        int horizontalRadius = AnimusConfig.rituals.solHorizontalRange.get();
        int configVerticalRange = AnimusConfig.rituals.solVerticalRange.get();

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

                        // Check if this is a valid dark spot
                        if (level.isEmptyBlock(checkPos) &&
                            level.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, checkPos) < 8 &&
                            level.getBlockState(checkPos.below()).isFaceSturdy(level, checkPos.below(), Direction.UP)) {

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
        return new RitualSol();
    }
}
