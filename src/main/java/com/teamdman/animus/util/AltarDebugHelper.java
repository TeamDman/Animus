package com.teamdman.animus.util;

import com.teamdman.animus.Animus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.altar.AltarComponent;
import wayoftime.bloodmagic.altar.AltarTier;
import wayoftime.bloodmagic.altar.ComponentType;
import wayoftime.bloodmagic.common.tile.TileAltar;

/**
 * Debug helper to diagnose why tier 6 altars aren't being recognized
 */
public class AltarDebugHelper {

    public static void debugTier6Validation(TileAltar altar, BlockPos altarPos, Level level) {
        Animus.LOGGER.info("=== TIER 6 ALTAR DEBUG ===");
        Animus.LOGGER.info("Altar position: {}", altarPos);
        Animus.LOGGER.info("Current altar tier: {}", altar.getTier());

        // Get tier 6 structure
        AltarTier tier6 = AltarTier.values()[5]; // SIX
        Animus.LOGGER.info("Checking tier 6 structure with {} components", tier6.getAltarComponents().size());

        // Check only CRYSTAL components
        int crystalCount = 0;
        int validCrystals = 0;

        for (AltarComponent component : tier6.getAltarComponents()) {
            if (component.getComponent() == ComponentType.CRYSTAL) {
                crystalCount++;
                BlockPos componentPos = altarPos.offset(component.getOffset());
                BlockState actualState = level.getBlockState(componentPos);

                // Get what Blood Magic expects
                var validStates = wayoftime.bloodmagic.impl.BloodMagicAPI.INSTANCE.getComponentStates(ComponentType.CRYSTAL);

                Animus.LOGGER.info("CRYSTAL #{}: position={}", crystalCount, componentPos);
                Animus.LOGGER.info("  Actual block: {}", actualState.getBlock());
                Animus.LOGGER.info("  Actual state: {}", actualState);
                Animus.LOGGER.info("  Valid states count: {}", validStates.size());
                Animus.LOGGER.info("  Valid states: {}", validStates);
                Animus.LOGGER.info("  Is valid: {}", validStates.contains(actualState));

                if (validStates.contains(actualState)) {
                    validCrystals++;
                }
            }
        }

        Animus.LOGGER.info("CRYSTAL summary: {}/{} valid", validCrystals, crystalCount);
        Animus.LOGGER.info("=== END TIER 6 DEBUG ===");
    }
}
