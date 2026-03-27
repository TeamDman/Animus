package com.breakinblocks.animusnv.rituals;

import net.minecraft.core.BlockPos;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;

public final class RitualAreaDescriptors {

    private RitualAreaDescriptors() {
    }

    public static AreaDescriptor singleBlockAbove() {
        return new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1, 1, 1);
    }

    public static AreaDescriptor largeCube65() {
        return new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65, 65, 65);
    }

    public static AreaDescriptor smallCube5() {
        return new AreaDescriptor.Rectangle(new BlockPos(-2, -2, -2), 5, 5, 5);
    }

    /**
     * @param radius Distance from center in each direction (total size = radius*2 + 1)
     */
    public static AreaDescriptor symmetricCube(int radius) {
        int size = radius * 2 + 1;
        return new AreaDescriptor.Rectangle(new BlockPos(-radius, -radius, -radius), size, size, size);
    }

    public static AreaDescriptor horizontalArea(int horizontalRadius, int verticalRadius) {
        int hSize = horizontalRadius * 2 + 1;
        int vSize = verticalRadius * 2 + 1;
        return new AreaDescriptor.Rectangle(
            new BlockPos(-horizontalRadius, -verticalRadius, -horizontalRadius),
            hSize, vSize, hSize
        );
    }

    public static AreaDescriptor altarSearch(int horizontalRadius, int verticalRadius) {
        return horizontalArea(horizontalRadius, verticalRadius);
    }
}
