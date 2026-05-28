package com.breakinblocks.animusnv.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class AnimusBlockProperties {

    private AnimusBlockProperties() {
    }

    public static BlockBehaviour.Properties bloodWoodLog() {
        return BlockBehaviour.Properties.of()
            .strength(2.0F)
            .sound(SoundType.WOOD);
    }

    public static BlockBehaviour.Properties bloodWoodPlanks() {
        return BlockBehaviour.Properties.of()
            .strength(2.0F, 3.0F)
            .sound(SoundType.WOOD);
    }

    public static BlockBehaviour.Properties willfulStone() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops();
    }

    public static BlockBehaviour.Properties willfulStone(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
            .mapColor(mapColor)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops();
    }

    public static BlockBehaviour.Properties crystallizedSpiritus() {
        return BlockBehaviour.Properties.of()
            .strength(5.0F, 10.0F)
            .sound(SoundType.AMETHYST)
            .requiresCorrectToolForDrops();
    }
}
