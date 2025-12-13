package com.teamdman.animus.compat.arsnouveau;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.ModList;

/**
 * Helper class for interacting with Ars Nouveau Source Jars
 * TODO: Implement when Ars Nouveau updates to 1.21
 */
public class SourceJarHelper {

    /**
     * Check if Ars Nouveau is loaded
     */
    public static boolean isArsNouveauLoaded() {
        return ModList.get().isLoaded("ars_nouveau");
    }

    /**
     * Attempts to drain source from a Source Jar placed above the given position
     * @param level The server level
     * @param belowPos The position below the source jar (e.g., ritual stone position)
     * @param maxDrain Maximum amount of source to drain
     * @return The amount of source actually drained
     */
    public static int drainSourceFromJarAbove(ServerLevel level, BlockPos belowPos, int maxDrain) {
        if (!isArsNouveauLoaded()) {
            return 0;
        }

        // TODO: Implement when Ars Nouveau is available for 1.21
        // This would check for a Source Jar at belowPos.above() and drain source from it
        return 0;
    }
}
