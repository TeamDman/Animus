package com.breakinblocks.animusnv.advancements;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.neovitae.api.altar.IAraVitae;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Map;

/**
 * Awards altar tier advancements to the player who completed the structure.
 *
 * NeoVitae recalculates an altar's tier lazily, so the surrounding altars are asked to
 * revalidate as soon as a player finishes placing a block near them.
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class AltarTierAdvancementHandler {

    private static final int SEARCH_RADIUS = 8;

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        LevelAccessor level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        BlockPos placedAt = event.getPos();
        int chunkX = placedAt.getX() >> 4;
        int chunkZ = placedAt.getZ() >> 4;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunk(chunkX + dx, chunkZ + dz, false);
                if (chunk == null) {
                    continue;
                }

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    if (!(entry.getValue() instanceof IAraVitae altar)) {
                        continue;
                    }
                    if (!entry.getKey().closerThan(placedAt, SEARCH_RADIUS)) {
                        continue;
                    }

                    altar.checkTier();
                    int tier = altar.getTier();
                    if (tier > 0) {
                        AnimusCriteriaTriggers.ALTAR_TIER.get().trigger(player, tier);
                    }
                }
            }
        }
    }
}
