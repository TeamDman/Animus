package com.breakinblocks.animusnv.events;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.items.sigils.effects.EquivalencySigilEffect;
import com.breakinblocks.animusnv.items.sigils.effects.HeavenlyWrathSigilEffect;
import com.breakinblocks.animusnv.items.sigils.effects.StormSigilEffect;
import com.breakinblocks.animusnv.items.sigils.effects.TemporalDominanceSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.rituals.RitualSerenity;
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

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class WorldEffectEventHandler {

    /**
     * Convert Essentia Vitae to AntiLife when struck by lightning.
     */
    @SubscribeEvent
    public static void onLightningStrike(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof LightningBolt) || event.getLevel().isClientSide()) {
            return;
        }

        LightningBolt lightning = (LightningBolt) entity;
        Level level = event.getLevel();
        BlockPos strikePos = lightning.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
            strikePos.offset(-1, -1, -1),
            strikePos.offset(1, 1, 1)
        )) {
            BlockState state = level.getBlockState(pos);

            if (state.getBlock().getDescriptionId().contains("life_essence")) {
                level.setBlock(pos, AnimusBlocks.BLOCK_FLUID_ANTILIFE.get().defaultBlockState(), 3);
                level.scheduleTick(pos, AnimusBlocks.BLOCK_FLUID_ANTILIFE.get(), 1);
            }
        }
    }

    @SubscribeEvent
    public static void onMobSpawnCheck(FinalizeSpawnEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getSpawnType() == MobSpawnType.SPAWNER) {
            return;
        }

        if (event.getSpawnType() == MobSpawnType.CONVERSION) {
            return;
        }

        // Skip WorldGenRegion - only process actual Level instances
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }

        BlockPos spawnPos = BlockPos.containing(event.getX(), event.getY(), event.getZ());

        if (RitualSerenity.isInSerenityZone(level, spawnPos)) {
            event.setSpawnCancelled(true);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            StormSigilEffect.tickPendingSpawns(serverLevel);
            HeavenlyWrathSigilEffect.tickPendingFalls(serverLevel);
            TemporalDominanceSigilEffect.tickAcceleratedBlocks(serverLevel);
            EquivalencySigilEffect.tickReplacements(serverLevel);

            if (serverLevel.getGameTime() % 10 == 0) {
                TemporalDominanceSigilEffect.syncToClients(serverLevel);
            }
        }
    }
}
