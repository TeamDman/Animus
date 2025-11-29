package com.teamdman.animus.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.altar.AltarComponent;
import wayoftime.bloodmagic.altar.AltarTier;
import wayoftime.bloodmagic.altar.ComponentType;
import wayoftime.bloodmagic.common.tile.TileAltar;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for determining altar upgrade requirements using Blood Magic's actual multiblock data
 */
public class AltarUpgradeHelper {

    /**
     * Gets the ghost blocks needed to upgrade an altar to the next tier
     * Uses Blood Magic's own altar structure definitions for accuracy
     * @param altar The altar to check
     * @param altarPos The position of the altar block
     * @return Map of positions to block IDs for ghost rendering
     */
    public static Map<BlockPos, ResourceLocation> getUpgradeBlocks(TileAltar altar, BlockPos altarPos) {
        Map<BlockPos, ResourceLocation> ghostBlocks = new HashMap<>();

        int currentTier = altar.getTier();
        if (currentTier >= 6) {
            // Already max tier
            return ghostBlocks;
        }

        // Get the next tier's structure
        AltarTier nextTier = AltarTier.values()[currentTier];

        // Get all components for the next tier from Blood Magic's data
        for (AltarComponent component : nextTier.getAltarComponents()) {
            BlockPos componentPos = altarPos.offset(component.getOffset());
            ResourceLocation blockId = getBlockForComponentType(component.getComponent());

            if (blockId != null) {
                ghostBlocks.put(componentPos, blockId);
            }
        }

        return ghostBlocks;
    }

    /**
     * Maps Blood Magic's ComponentType to actual block IDs
     */
    private static ResourceLocation getBlockForComponentType(ComponentType type) {
        return switch (type) {
            case BLOODRUNE -> ResourceLocation.fromNamespaceAndPath("bloodmagic", "blankrune");
            case BLOODSTONE -> ResourceLocation.fromNamespaceAndPath("bloodmagic", "largebloodstonebrick");
            case CRYSTAL -> ResourceLocation.fromNamespaceAndPath("animus", "crystallized_demon_will_block"); // Our custom block
            case GLOWSTONE -> ResourceLocation.fromNamespaceAndPath("minecraft", "glowstone");
            case BEACON -> ResourceLocation.fromNamespaceAndPath("bloodmagic", "dungeon_metal");
            case NOTAIR -> ResourceLocation.fromNamespaceAndPath("minecraft", "stone_bricks"); // Any solid block works
        };
    }
}
