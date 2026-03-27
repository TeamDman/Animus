package com.breakinblocks.animusnv.util;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Helper methods for the Ritual of Culling.
 * Contains boss detection, fake-player drop simulation, and kill-effect logic
 * extracted from RitualCulling to keep the ritual class focused on orchestration.
 */
public final class CullingHelper {

    private CullingHelper() {}

    /**
     * Determines whether a living entity should be treated as a boss for culling purposes.
     * Bosses are invulnerable entities, withers, ender dragons, and raid mobs.
     */
    public static boolean isBoss(LivingEntity entity) {
        return entity.isInvulnerable()
               || entity.getType() == net.minecraft.world.entity.EntityType.WITHER
               || entity.getType() == net.minecraft.world.entity.EntityType.ENDER_DRAGON
               || entity.getType().is(net.minecraft.tags.EntityTypeTags.RAIDERS);
    }

    /**
     * Determines whether an entity should be skipped by the culling ritual.
     * Skips players with health above 4, entities in the disallow_culling tag,
     * and entities with active potion effects (unless canKillBuffedMobs is enabled).
     *
     * @return true if the entity should be skipped
     */
    public static boolean shouldSkipEntity(LivingEntity entity) {
        if (entity instanceof Player && entity.getHealth() > 4) {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]:   SKIPPED - Player with health > 4");
            }
            return true;
        }

        if (entity.getType().is(Constants.Tags.DISALLOW_CULLING)) {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]:   SKIPPED - Entity in disallow_culling tag");
            }
            return true;
        }

        var effects = entity.getActiveEffects();
        if (AnimusConfig.rituals.cullingDebug.get()) {
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Active effects: {}", effects.size());
            if (!effects.isEmpty()) {
                for (var effect : effects) {
                    Animus.LOGGER.debug("[Ritual of Culling Debug]:     - {}", effect.getEffect().value().getDescriptionId());
                }
            }
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   canKillBuffedMobs config: {}", AnimusConfig.general.canKillBuffedMobs.get());
        }

        if (!effects.isEmpty() && !AnimusConfig.general.canKillBuffedMobs.get()) {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]:   SKIPPED - Entity has potion effects and canKillBuffedMobs is false");
            }
            return true;
        }

        return false;
    }

    /**
     * Removes all primed TNT entities within the given area if the cullingKillsTnT config is enabled.
     *
     * @param level the level to search in
     * @param range the bounding box to search for TNT
     */
    public static void removePrimedTnt(Level level, AABB range) {
        if (!AnimusConfig.rituals.cullingKillsTnT.get()) {
            return;
        }
        List<PrimedTnt> tntList = level.getEntitiesOfClass(PrimedTnt.class, range);
        for (PrimedTnt tnt : tntList) {
            tnt.setFuse(1000);
            tnt.discard();
            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]: Found TNT entity, killing");
            }
        }
    }

    /**
     * Attempts to make a boss entity vulnerable if the required conditions are met:
     * killBoss config enabled, 100+ destructive Spiritus, and sufficient EV.
     * Returns true if the boss was made vulnerable and can be killed.
     *
     * @param entity         the boss entity
     * @param currentAmount  current destructive Spiritus amount
     * @param currentEV current EV in the Anima
     * @param requiredEssence the EV needed (boss cost + refresh cost * entity count)
     * @return true if the boss was made vulnerable
     */
    public static boolean tryMakeBossVulnerable(LivingEntity entity, double currentAmount, int currentEV, int requiredEssence) {
        if (AnimusConfig.rituals.killBoss.get() && currentAmount > 99 && currentEV >= requiredEssence) {
            if (AnimusConfig.rituals.cullingDebug.get()) {
                Animus.LOGGER.debug("[Ritual of Culling Debug]:   Boss kill conditions met - making vulnerable");
                Animus.LOGGER.debug("[Ritual of Culling Debug]:     Current Spiritus: {}", currentAmount);
                Animus.LOGGER.debug("[Ritual of Culling Debug]:     Boss cost: {}", AnimusConfig.rituals.bossCost.get());
            }
            entity.setInvulnerable(false);
            return true;
        }

        if (AnimusConfig.rituals.cullingDebug.get()) {
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Boss kill conditions NOT met:");
            Animus.LOGGER.debug("[Ritual of Culling Debug]:     killBoss config: {}", AnimusConfig.rituals.killBoss.get());
            Animus.LOGGER.debug("[Ritual of Culling Debug]:     Current Spiritus: {} (need > 99)", currentAmount);
            Animus.LOGGER.debug("[Ritual of Culling Debug]:     Current essence: {}", currentEV);
            Animus.LOGGER.debug("[Ritual of Culling Debug]:     Required essence: {}", requiredEssence);
        }
        return false;
    }

    /**
     * Applies damage to a living entity using a fake player, simulating a player kill
     * so that loot tables produce player-only drops (e.g. blaze rods).
     * Sets {@code lastHurtByPlayer} and {@code lastHurtByPlayerTime} via reflection,
     * then applies player-attack damage from a fake player holding a looting sword.
     *
     * @param entity      the entity to damage
     * @param serverLevel the server level
     * @param ritualOwner the UUID of the ritual owner (used for the fake player profile)
     * @param ritualPos   position of the master ritual stone (used for looting sword enchantment)
     * @param damage      the amount of damage to deal
     * @return true if the entity was successfully hurt
     */
    public static boolean applyPlayerKillDamage(LivingEntity entity, ServerLevel serverLevel, UUID ritualOwner, BlockPos ritualPos, float damage) {
        AnimusFakePlayer fakePlayer = AnimusFakePlayer.get(serverLevel, ritualOwner, null);

        ItemStack lootingSword = AnimusFakePlayer.createLootingSword(serverLevel, ritualPos);
        fakePlayer.setItemInHand(InteractionHand.MAIN_HAND, lootingSword);
        fakePlayer.setPos(entity.getX(), entity.getY(), entity.getZ());

        // Set lastHurtByPlayer so loot tables treat this as a player kill (enables player-only drops like blaze rods)
        try {
            java.lang.reflect.Field lastHurtByPlayerField = LivingEntity.class.getDeclaredField("lastHurtByPlayer");
            lastHurtByPlayerField.setAccessible(true);
            lastHurtByPlayerField.set(entity, fakePlayer);

            java.lang.reflect.Field lastHurtByPlayerTimeField = LivingEntity.class.getDeclaredField("lastHurtByPlayerTime");
            lastHurtByPlayerTimeField.setAccessible(true);
            lastHurtByPlayerTimeField.setInt(entity, 100);
        } catch (Exception e) {
            // Fall back to obfuscated field names
            try {
                java.lang.reflect.Field lastHurtByPlayerField = LivingEntity.class.getDeclaredField("f_20889_");
                lastHurtByPlayerField.setAccessible(true);
                lastHurtByPlayerField.set(entity, fakePlayer);

                java.lang.reflect.Field lastHurtByPlayerTimeField = LivingEntity.class.getDeclaredField("f_20890_");
                lastHurtByPlayerTimeField.setAccessible(true);
                lastHurtByPlayerTimeField.setInt(entity, 100);
            } catch (Exception e2) {
                if (AnimusConfig.rituals.cullingDebug.get()) {
                    Animus.LOGGER.debug("[Ritual of Culling Debug]: Failed to set lastHurtByPlayer fields: {}", e2.getMessage());
                }
            }
        }

        DamageSource playerDamage = serverLevel.damageSources().playerAttack(fakePlayer);
        return entity.hurt(playerDamage, damage);
    }

    /**
     * Logs entity processing details if culling debug is enabled.
     */
    public static void debugLogEntityProcessing(LivingEntity entity) {
        if (AnimusConfig.rituals.cullingDebug.get()) {
            Animus.LOGGER.debug("[Ritual of Culling Debug]: Processing entity: {} at {} (Type: {})", entity.getName().getString(), entity.blockPosition(), entity.getType());
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Health: {}/{}", entity.getHealth(), entity.getMaxHealth());
        }
    }

    /**
     * Logs boss status and kill attempt details if culling debug is enabled.
     */
    public static void debugLogKillAttempt(LivingEntity entity, boolean isBoss, float damage, boolean usePlayerKill) {
        if (AnimusConfig.rituals.cullingDebug.get()) {
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Is boss: {}", isBoss);
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Is invulnerable: {}", entity.isInvulnerable());
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   ATTEMPTING TO KILL entity");
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Applying damage: {}", damage);
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Using player kill: {}", usePlayerKill);
        }
    }

    /**
     * Logs kill result details if culling debug is enabled.
     */
    public static void debugLogKillResult(LivingEntity entity, boolean result) {
        if (AnimusConfig.rituals.cullingDebug.get()) {
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Damage result: {}", result);
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Entity alive after damage: {}", entity.isAlive());
            Animus.LOGGER.debug("[Ritual of Culling Debug]:   Entity removed: {}", entity.isRemoved());
        }
    }

    /**
     * Calculates the will buffer contribution from killing an entity.
     * Animals contribute more will than other mobs.
     *
     * @param entity the killed entity
     * @return the will amount to add to the buffer
     */
    public static double calculateWillGain(LivingEntity entity) {
        double modifier = (entity instanceof net.minecraft.world.entity.animal.Animal) ? 2.0 : 0.5;
        return modifier * Math.min(15.0, entity.getMaxHealth());
    }

    /**
     * Spawns kill-effect particles and plays a sound at the entity's position.
     */
    public static void spawnKillEffects(Level level, BlockPos entityPos, Random rand) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                entityPos.getX() + 0.5,
                entityPos.getY() + 0.5,
                entityPos.getZ() + 0.5,
                rand.nextInt(4),
                (rand.nextDouble() - 0.5D) * 2.0D,
                rand.nextDouble(),
                (rand.nextDouble() - 0.5D) * 2.0D,
                0.1
            );
        }

        level.playSound(null, entityPos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
