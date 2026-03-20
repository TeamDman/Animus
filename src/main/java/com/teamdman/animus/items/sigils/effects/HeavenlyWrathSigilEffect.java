package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Sigil of Heavenly Wrath - levitates enemies then drops them for fall damage.
 * <p>
 * Features:
 * - Levitates all enemies in 16 block radius (not the user)
 * - Applies upward velocity boost for 100% faster levitation
 * - After 3 seconds, removes levitation and applies downward velocity
 * - Flying entities get stronger downward velocity based on height from ground
 */
public record HeavenlyWrathSigilEffect() implements ISigilEffect {
    public static final MapCodec<HeavenlyWrathSigilEffect> CODEC = MapCodec.unit(HeavenlyWrathSigilEffect::new);

    // Track pending fall effects
    private static final List<PendingFall> pendingFalls = new ArrayList<>();

    private static class PendingFall {
        final ServerLevel level;
        final UUID entityUUID;
        final UUID playerUUID;
        final long fallTick;
        final double heightFromGround;

        PendingFall(ServerLevel level, UUID entityUUID, UUID playerUUID, long fallTick, double heightFromGround) {
            this.level = level;
            this.entityUUID = entityUUID;
            this.playerUUID = playerUUID;
            this.fallTick = fallTick;
            this.heightFromGround = heightFromGround;
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

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        // Get all living entities in a 16 block radius
        Vec3 playerPos = player.position();
        AABB area = AABB.ofSize(playerPos, 32.0, 32.0, 32.0);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

        long currentTick = serverLevel.getServer().getTickCount();
        long fallTick = currentTick + 60; // 3 seconds

        boolean affectedAny = false;
        for (LivingEntity entity : entities) {
            // Skip the player who used the sigil
            if (entity == player) {
                continue;
            }

            // Apply levitation effect for 3 seconds
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1));

            // Apply upward velocity boost
            Vec3 currentVelocity = entity.getDeltaMovement();
            entity.setDeltaMovement(currentVelocity.x, currentVelocity.y + 0.8, currentVelocity.z);
            entity.hurtMarked = true;

            // Calculate height from ground
            double heightFromGround = calculateHeightFromGround(entity);

            // Schedule the fall effect
            pendingFalls.add(new PendingFall(
                    serverLevel,
                    entity.getUUID(),
                    player.getUUID(),
                    fallTick,
                    heightFromGround
            ));

            affectedAny = true;
        }

        return affectedAny;
    }

    /**
     * Calculate how far an entity is from the ground.
     */
    private double calculateHeightFromGround(LivingEntity entity) {
        double entityY = entity.getY();
        double groundY = entityY;

        Level level = entity.level();
        for (int y = (int) entityY; y >= level.getMinBuildHeight(); y--) {
            if (level.getBlockState(entity.blockPosition().atY(y)).isSolid()) {
                groundY = y + 1;
                break;
            }
        }

        return entityY - groundY;
    }

    /**
     * Process pending fall effects - should be called from a tick event.
     */
    public static void tickPendingFalls(ServerLevel level) {
        if (pendingFalls.isEmpty()) {
            return;
        }

        long currentTick = level.getServer().getTickCount();
        Iterator<PendingFall> iterator = pendingFalls.iterator();

        while (iterator.hasNext()) {
            PendingFall fall = iterator.next();
            if (fall.level == level && currentTick >= fall.fallTick) {
                LivingEntity entity = (LivingEntity) level.getEntity(fall.entityUUID);
                if (entity != null && entity.isAlive()) {
                    applyFallEffect(entity, fall.heightFromGround);
                }
                iterator.remove();
            }
        }
    }

    /**
     * Apply the fall effect to an entity.
     */
    private static void applyFallEffect(LivingEntity entity, double heightFromGround) {
        // Remove levitation
        entity.removeEffect(MobEffects.LEVITATION);

        // Apply NeoVitae's heavy_heart effect to prevent flight
        ResourceLocation heavyHeartRL = ResourceLocation.fromNamespaceAndPath("neovitae", "heavy_heart");
        var heavyHeartOpt = BuiltInRegistries.MOB_EFFECT.getOptional(heavyHeartRL);
        if (heavyHeartOpt.isPresent()) {
            entity.addEffect(new MobEffectInstance(Holder.direct(heavyHeartOpt.get()), 40, 4));
        }

        // Base downward velocity
        double downwardVelocity = -1.5;

        // Increase velocity for flying entities
        if (heightFromGround > 5.0) {
            double additionalVelocity = Math.min((heightFromGround / 5.0) * -0.5, -3.0);
            downwardVelocity += additionalVelocity;
        }

        // Apply the downward velocity
        Vec3 currentVelocity = entity.getDeltaMovement();
        entity.setDeltaMovement(currentVelocity.x * 0.5, downwardVelocity, currentVelocity.z * 0.5);
        entity.hurtMarked = true;
    }
}
