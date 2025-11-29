package com.teamdman.animus.util;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.altar.AltarTier;
import wayoftime.bloodmagic.altar.ComponentType;
import wayoftime.bloodmagic.common.tile.TileAltar;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for determining altar upgrade requirements
 */
public class AltarUpgradeHelper {

    /**
     * Gets the ghost blocks needed to upgrade an altar to the next tier
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

        AltarTier nextTier = AltarTier.values()[currentTier];

        // Get required components for next tier
        // This is based on Blood Magic's altar structure requirements

        // Rune block ID
        ResourceLocation runeBlock = ResourceLocation.fromNamespaceAndPath("bloodmagic", "blankrune");
        // Crystal block ID (Blood Altar Capstone/Crystal)
        ResourceLocation crystalBlock = ResourceLocation.fromNamespaceAndPath("bloodmagic", "largebloodstonebrick");

        // Add blocks based on next tier requirements
        switch (nextTier) {
            case TWO:
                // Tier 2: 8 runes in cardinal and diagonal positions around altar
                addRune(ghostBlocks, altarPos, runeBlock, 1, -1, 1);   // NE
                addRune(ghostBlocks, altarPos, runeBlock, -1, -1, 1);  // NW
                addRune(ghostBlocks, altarPos, runeBlock, 1, -1, -1);  // SE
                addRune(ghostBlocks, altarPos, runeBlock, -1, -1, -1); // SW
                addRune(ghostBlocks, altarPos, runeBlock, 0, -1, 2);   // N far
                addRune(ghostBlocks, altarPos, runeBlock, 0, -1, -2);  // S far
                addRune(ghostBlocks, altarPos, runeBlock, 2, -1, 0);   // E far
                addRune(ghostBlocks, altarPos, runeBlock, -2, -1, 0);  // W far
                break;

            case THREE:
                // Tier 3: Additional runes in second ring
                addRune(ghostBlocks, altarPos, runeBlock, 2, -1, 2);   // NE far
                addRune(ghostBlocks, altarPos, runeBlock, -2, -1, 2);  // NW far
                addRune(ghostBlocks, altarPos, runeBlock, 2, -1, -2);  // SE far
                addRune(ghostBlocks, altarPos, runeBlock, -2, -1, -2); // SW far
                addRune(ghostBlocks, altarPos, runeBlock, 1, -1, 3);   // N mid
                addRune(ghostBlocks, altarPos, runeBlock, -1, -1, 3);  // N mid
                addRune(ghostBlocks, altarPos, runeBlock, 1, -1, -3);  // S mid
                addRune(ghostBlocks, altarPos, runeBlock, -1, -1, -3); // S mid
                addRune(ghostBlocks, altarPos, runeBlock, 3, -1, 1);   // E mid
                addRune(ghostBlocks, altarPos, runeBlock, 3, -1, -1);  // E mid
                addRune(ghostBlocks, altarPos, runeBlock, -3, -1, 1);  // W mid
                addRune(ghostBlocks, altarPos, runeBlock, -3, -1, -1); // W mid
                break;

            case FOUR:
                // Tier 4: 4 pillars with blood stone above runes
                addPillar(ghostBlocks, altarPos, crystalBlock, 3, 3);   // NE pillar
                addPillar(ghostBlocks, altarPos, crystalBlock, -3, 3);  // NW pillar
                addPillar(ghostBlocks, altarPos, crystalBlock, 3, -3);  // SE pillar
                addPillar(ghostBlocks, altarPos, crystalBlock, -3, -3); // SW pillar
                break;

            case FIVE:
                // Tier 5: Additional runes and crystals in outer ring
                addRune(ghostBlocks, altarPos, runeBlock, 0, -1, 4);   // N far
                addRune(ghostBlocks, altarPos, runeBlock, 0, -1, -4);  // S far
                addRune(ghostBlocks, altarPos, runeBlock, 4, -1, 0);   // E far
                addRune(ghostBlocks, altarPos, runeBlock, -4, -1, 0);  // W far
                addRune(ghostBlocks, altarPos, runeBlock, 3, -1, 3);   // NE far
                addRune(ghostBlocks, altarPos, runeBlock, -3, -1, 3);  // NW far
                addRune(ghostBlocks, altarPos, runeBlock, 3, -1, -3);  // SE far
                addRune(ghostBlocks, altarPos, runeBlock, -3, -1, -3); // SW far
                // Crystals on cardinals
                ghostBlocks.put(altarPos.offset(0, 2, 5), crystalBlock);  // N
                ghostBlocks.put(altarPos.offset(0, 2, -5), crystalBlock); // S
                ghostBlocks.put(altarPos.offset(5, 2, 0), crystalBlock);  // E
                ghostBlocks.put(altarPos.offset(-5, 2, 0), crystalBlock); // W
                break;

            case SIX:
                // Tier 6: Large crystals in diagonal positions
                ghostBlocks.put(altarPos.offset(4, 2, 4), crystalBlock);   // NE
                ghostBlocks.put(altarPos.offset(-4, 2, 4), crystalBlock);  // NW
                ghostBlocks.put(altarPos.offset(4, 2, -4), crystalBlock);  // SE
                ghostBlocks.put(altarPos.offset(-4, 2, -4), crystalBlock); // SW
                // Additional runes in outer positions
                addRune(ghostBlocks, altarPos, runeBlock, 2, -1, 4);
                addRune(ghostBlocks, altarPos, runeBlock, -2, -1, 4);
                addRune(ghostBlocks, altarPos, runeBlock, 2, -1, -4);
                addRune(ghostBlocks, altarPos, runeBlock, -2, -1, -4);
                addRune(ghostBlocks, altarPos, runeBlock, 4, -1, 2);
                addRune(ghostBlocks, altarPos, runeBlock, 4, -1, -2);
                addRune(ghostBlocks, altarPos, runeBlock, -4, -1, 2);
                addRune(ghostBlocks, altarPos, runeBlock, -4, -1, -2);
                break;
        }

        return ghostBlocks;
    }

    private static void addRune(Map<BlockPos, ResourceLocation> ghostBlocks, BlockPos altarPos, ResourceLocation block, int x, int y, int z) {
        ghostBlocks.put(altarPos.offset(x, y, z), block);
    }

    private static void addPillar(Map<BlockPos, ResourceLocation> ghostBlocks, BlockPos altarPos, ResourceLocation block, int x, int z) {
        // Add pillar blocks (3 blocks high)
        ghostBlocks.put(altarPos.offset(x, 1, z), block);
        ghostBlocks.put(altarPos.offset(x, 2, z), block);
        ghostBlocks.put(altarPos.offset(x, 3, z), block);
    }
}
