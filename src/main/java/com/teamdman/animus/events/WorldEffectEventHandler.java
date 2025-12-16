package com.teamdman.animus.events;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.sigils.effects.EquivalencySigilEffect;
import com.teamdman.animus.items.sigils.effects.HeavenlyWrathSigilEffect;
import com.teamdman.animus.items.sigils.effects.StormSigilEffect;
import com.teamdman.animus.items.sigils.effects.TemporalDominanceSigilEffect;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.rituals.RitualSerenity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Event handler for world-level effects
 * Handles lightning conversion, mob spawn prevention, and periodic effect ticks
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class WorldEffectEventHandler {

    /**
     * Convert Life Essence to AntiLife when struck by lightning
     */
    @SubscribeEvent
    public static void onLightningStrike(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        // Only handle lightning bolts on server side
        if (!(entity instanceof LightningBolt) || event.getLevel().isClientSide()) {
            return;
        }

        LightningBolt lightning = (LightningBolt) entity;
        Level level = event.getLevel();
        BlockPos strikePos = lightning.blockPosition();

        // Check a 3x3x3 area around the lightning strike
        for (BlockPos pos : BlockPos.betweenClosed(
            strikePos.offset(-1, -1, -1),
            strikePos.offset(1, 1, 1)
        )) {
            BlockState state = level.getBlockState(pos);

            // Check if this block is Blood Magic's life essence fluid
            if (state.getBlock().getDescriptionId().contains("life_essence")) {
                // Convert to AntiLife fluid
                level.setBlock(pos, AnimusBlocks.BLOCK_FLUID_ANTILIFE.get().defaultBlockState(), 3);

                // Schedule tick so it starts spreading
                level.scheduleTick(pos, AnimusBlocks.BLOCK_FLUID_ANTILIFE.get(), 1);
            }
        }
    }

    /**
     * Prevent mob spawning in Ritual of Serenity zones
     */
    @SubscribeEvent
    public static void onMobSpawnCheck(FinalizeSpawnEvent event) {
        // Only run on server side
        if (event.getLevel().isClientSide()) {
            return;
        }

        // Allow spawner spawns to proceed
        if (event.getSpawnType() == MobSpawnType.SPAWNER) {
            return;
        }

        // Allow conversion spawns (e.g., villager -> zombie villager) to proceed
        if (event.getSpawnType() == MobSpawnType.CONVERSION) {
            return;
        }

        // Only process actual Level instances (not WorldGenRegion during chunk generation)
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }

        // Check if the spawn position is within a Serenity zone
        BlockPos spawnPos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

        if (RitualSerenity.isInSerenityZone(level, spawnPos)) {
            event.setSpawnCancelled(true);
        }
    }

    /**
     * Process pending effects from various sigils
     * Runs every server tick
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        // Only process server levels
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            StormSigilEffect.tickPendingSpawns(serverLevel);
            HeavenlyWrathSigilEffect.tickPendingFalls(serverLevel);
            TemporalDominanceSigilEffect.tickAcceleratedBlocks(serverLevel);
            EquivalencySigilEffect.tickReplacements(serverLevel);

            // Sync accelerated blocks to clients every 10 ticks (0.5 seconds)
            if (serverLevel.getGameTime() % 10 == 0) {
                TemporalDominanceSigilEffect.syncToClients(serverLevel);
            }
        }
    }
}
