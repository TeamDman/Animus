package com.teamdman.animus.blocks;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Centralized block property definitions for Animus blocks.
 * Eliminates duplication of common property patterns across block classes.
 */
public final class AnimusBlockProperties {

    private AnimusBlockProperties() {
        // Utility class - no instantiation
    }

    // ==================== Wood Block Properties ====================

    /**
     * Properties for Blood Wood logs (rotatable pillar blocks).
     * Hardness: 2.0F (same as vanilla logs)
     */
    public static BlockBehaviour.Properties bloodWoodLog() {
        return BlockBehaviour.Properties.of()
            .strength(2.0F)
            .sound(SoundType.WOOD);
    }

    /**
     * Properties for Blood Wood planks and derivatives (planks, fence, fence gate, slab, stairs).
     * Hardness: 2.0F, Blast Resistance: 3.0F (same as vanilla planks)
     */
    public static BlockBehaviour.Properties bloodWoodPlanks() {
        return BlockBehaviour.Properties.of()
            .strength(2.0F, 3.0F)
            .sound(SoundType.WOOD);
    }

    // ==================== Stone Block Properties ====================

    /**
     * Properties for Willful Stone (unbreakable by non-owners).
     * Uses bedrock-like strength values.
     */
    public static BlockBehaviour.Properties willfulStone() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops();
    }

    /**
     * Properties for colored Willful Stone variants.
     * @param mapColor The color for the map
     */
    public static BlockBehaviour.Properties willfulStone(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
            .mapColor(mapColor)
            .strength(-1.0F, 3600000.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops();
    }

    // ==================== Crystal Block Properties ====================

    /**
     * Properties for Crystallized Demon Will block.
     * A crystal block formed from raw demon will.
     */
    public static BlockBehaviour.Properties crystallizedDemonWill() {
        return BlockBehaviour.Properties.of()
            .strength(5.0F, 10.0F)
            .sound(SoundType.AMETHYST)
            .requiresCorrectToolForDrops();
    }
}
