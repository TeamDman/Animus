package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.AnimusConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public record StormSigilEffect() implements ISigilEffect {
    public static final MapCodec<StormSigilEffect> CODEC = MapCodec.unit(StormSigilEffect::new);

    private static final List<PendingFishSpawn> pendingSpawns = new ArrayList<>();

    private static class PendingFishSpawn {
        final ServerLevel level;
        final BlockPos pos;
        final UUID playerUUID;
        final long spawnTick;

        PendingFishSpawn(ServerLevel level, BlockPos pos, UUID playerUUID, long spawnTick) {
            this.level = level;
            this.pos = pos;
            this.playerUUID = playerUUID;
            this.spawnTick = spawnTick;
        }
    }

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return false;
        }

        // Raycast to find target position
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endVec = eyePos.add(lookVec.scale(64.0));

        BlockHitResult result = level.clip(new ClipContext(
                eyePos,
                endVec,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player
        ));

        if (result.getType() == HitResult.Type.MISS) {
            return false;
        }

        BlockPos pos = result.getBlockPos();

        // Spawn lightning
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            lightning.setVisualOnly(false);
            level.addFreshEntity(lightning);
        }

        // Fish spawning when targeting water
        if (level instanceof ServerLevel serverLevel) {
            if (level.getFluidState(pos).is(Fluids.WATER) || level.getBlockState(pos).is(Blocks.WATER)) {
                int minLoot = AnimusConfig.sigils.stormFishLootMin.get();
                int maxLoot = AnimusConfig.sigils.stormFishLootMax.get();

                // Only spawn fish if not disabled (both set to 0)
                if (minLoot > 0 || maxLoot > 0) {
                    int actualMin = Math.min(minLoot, maxLoot);
                    int actualMax = Math.max(minLoot, maxLoot);

                    // Schedule fishing loot to spawn 20 ticks after lightning
                    int spawnCount = actualMin + level.random.nextInt(Math.max(1, actualMax - actualMin + 1));
                    BlockPos spawnPos = pos.above();
                    long spawnTick = serverLevel.getServer().getTickCount() + 20;

                    for (int i = 0; i < spawnCount; i++) {
                        pendingSpawns.add(new PendingFishSpawn(serverLevel, spawnPos, player.getUUID(), spawnTick));
                    }
                }
            }

            // Area damage during rain
            if (level.isRaining() && level.canSeeSky(pos)) {
                AABB damageArea = new AABB(pos).inflate(5.0);
                level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, damageArea).forEach(entity -> {
                    if (entity != player && level.canSeeSky(entity.blockPosition())) {
                        entity.hurt(level.damageSources().lightningBolt(), 4.0F);
                    }
                });
            }
        }

        return true;
    }

    public static void tickPendingSpawns(ServerLevel level) {
        if (pendingSpawns.isEmpty()) {
            return;
        }

        long currentTick = level.getServer().getTickCount();
        Iterator<PendingFishSpawn> iterator = pendingSpawns.iterator();

        while (iterator.hasNext()) {
            PendingFishSpawn spawn = iterator.next();
            if (spawn.level == level && currentTick >= spawn.spawnTick) {
                Player player = level.getPlayerByUUID(spawn.playerUUID);
                spawnFishingLoot(spawn.level, spawn.pos, player);
                iterator.remove();
            }
        }
    }

    private static void spawnFishingLoot(ServerLevel level, BlockPos pos, Player player) {
        ResourceLocation fishingLootTable = ResourceLocation.fromNamespaceAndPath("minecraft", "gameplay/fishing");
        LootTable lootTable = level.getServer().reloadableRegistries()
                .getLootTable(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.LOOT_TABLE, fishingLootTable));

        LootParams.Builder paramsBuilder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);

        if (player != null) {
            paramsBuilder.withParameter(LootContextParams.THIS_ENTITY, player);
        }

        LootParams params = paramsBuilder.create(LootContextParamSets.FISHING);
        List<ItemStack> loot = lootTable.getRandomItems(params);

        for (ItemStack item : loot) {
            if (!item.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        item
                );
                itemEntity.setDeltaMovement(
                        (level.random.nextDouble() - 0.5) * 0.2,
                        0.2,
                        (level.random.nextDouble() - 0.5) * 0.2
                );
                level.addFreshEntity(itemEntity);
            }
        }
    }
}
