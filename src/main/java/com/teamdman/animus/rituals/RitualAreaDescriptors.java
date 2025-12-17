package com.teamdman.animus.rituals;

import net.minecraft.core.BlockPos;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;

/**
 * Common area descriptor constants for ritual block ranges
 * Reduces duplication across ritual implementations
 */
public final class RitualAreaDescriptors {

    private RitualAreaDescriptors() {
        // Utility class
    }

    /**
     * Single block directly above the ritual stone
     * Position: (0, 1, 0), Size: 1x1x1
     * Common for rituals that interact with inventories, fluid containers, or other blocks
     */
    public static AreaDescriptor singleBlockAbove() {
        return new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1, 1, 1);
    }

    /**
     * Large 65x65x65 cube centered on ritual (32 blocks in each direction)
     * Common for area scanning rituals like Sol and Luna
     */
    public static AreaDescriptor largeCube65() {
        return new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65, 65, 65);
    }

    /**
     * Small 5x5x5 cube centered on ritual (2 blocks in each direction)
     * Common for focused effect rituals
     */
    public static AreaDescriptor smallCube5() {
        return new AreaDescriptor.Rectangle(new BlockPos(-2, -2, -2), 5, 5, 5);
    }

    /**
     * Creates a symmetric cube centered on the ritual
     * @param radius Distance from center in each direction (total size = radius*2 + 1)
     */
    public static AreaDescriptor symmetricCube(int radius) {
        int size = radius * 2 + 1;
        return new AreaDescriptor.Rectangle(new BlockPos(-radius, -radius, -radius), size, size, size);
    }

    /**
     * Creates a horizontal area with separate vertical range
     * Useful for rituals that scan different horizontal vs vertical distances
     * @param horizontalRadius Horizontal distance from center
     * @param verticalRadius Vertical distance from center
     */
    public static AreaDescriptor horizontalArea(int horizontalRadius, int verticalRadius) {
        int hSize = horizontalRadius * 2 + 1;
        int vSize = verticalRadius * 2 + 1;
        return new AreaDescriptor.Rectangle(
            new BlockPos(-horizontalRadius, -verticalRadius, -horizontalRadius),
            hSize, vSize, hSize
        );
    }

    /**
     * Creates an altar search range - typically wider horizontal than vertical
     * @param horizontalRadius Horizontal search distance
     * @param verticalRadius Vertical search distance
     */
    public static AreaDescriptor altarSearch(int horizontalRadius, int verticalRadius) {
        return horizontalArea(horizontalRadius, verticalRadius);
    }
}
