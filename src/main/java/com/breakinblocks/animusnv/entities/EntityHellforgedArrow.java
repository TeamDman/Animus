package com.breakinblocks.animusnv.entities;

import com.breakinblocks.animusnv.registry.AnimusEntityTypes;
import com.breakinblocks.animusnv.registry.AnimusSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;

/**
 * Hellforged Arrow Entity
 * Virtual arrow fired by the Hellforged Bow
 * - Pierces through all entities, only stops on blocks
 * - Deals charged damage based on charge time
 * - Execute mechanic: instant kill targets below HP threshold at full charge
 * - Applies status effects based on Spiritus type
 */
public class EntityHellforgedArrow extends AbstractArrow {
    private static final EntityDataAccessor<String> ID_SPIRITUS_TYPE =
        SynchedEntityData.defineId(EntityHellforgedArrow.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ID_SPIRITUS_LEVEL =
        SynchedEntityData.defineId(EntityHellforgedArrow.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ID_CHARGE_MULTIPLIER =
        SynchedEntityData.defineId(EntityHellforgedArrow.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ID_EXECUTE_THRESHOLD =
        SynchedEntityData.defineId(EntityHellforgedArrow.class, EntityDataSerializers.FLOAT);

    private static final int[] poisonTime = {40, 60, 100, 140, 200};
    private static final int[] poisonLevel = {0, 0, 1, 1, 2};
    private static final int[] slowTime = {60, 100, 140, 180, 240};
    private static final int[] slowLevel = {0, 1, 1, 2, 2};
    private static final int[] witherTime = {40, 60, 80, 100, 120};
    private static final int[] witherLevel = {0, 0, 1, 1, 2};

    public EntityHellforgedArrow(EntityType<? extends EntityHellforgedArrow> entityType, Level level) {
        super(entityType, level);
        this.pickup = Pickup.DISALLOWED;
    }

    public EntityHellforgedArrow(Level level, LivingEntity shooter) {
        super(AnimusEntityTypes.HELLFORGED_ARROW.get(), shooter, level, ItemStack.EMPTY, ItemStack.EMPTY);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SPIRITUS_TYPE, SpiritusType.RAW.toString());
        builder.define(ID_SPIRITUS_LEVEL, 0);
        builder.define(ID_CHARGE_MULTIPLIER, 0.0F);
        builder.define(ID_EXECUTE_THRESHOLD, 0.0F);
    }

    public void setSpiritusType(SpiritusType type) {
        this.entityData.set(ID_SPIRITUS_TYPE, type.toString());
    }

    public SpiritusType getSpiritusType() {
        try {
            return SpiritusType.valueOf(this.entityData.get(ID_SPIRITUS_TYPE).toUpperCase());
        } catch (IllegalArgumentException e) {
            return SpiritusType.RAW;
        }
    }

    public void setSpiritusLevel(int level) {
        this.entityData.set(ID_SPIRITUS_LEVEL, level);
    }

    public int getSpiritusLevel() {
        return this.entityData.get(ID_SPIRITUS_LEVEL);
    }

    public void setChargeMultiplier(float multiplier) {
        this.entityData.set(ID_CHARGE_MULTIPLIER, multiplier);
    }

    public float getChargeMultiplier() {
        return this.entityData.get(ID_CHARGE_MULTIPLIER);
    }

    public void setExecuteThreshold(double threshold) {
        this.entityData.set(ID_EXECUTE_THRESHOLD, (float) threshold);
    }

    public float getExecuteThreshold() {
        return this.entityData.get(ID_EXECUTE_THRESHOLD);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();

        if (entity instanceof LivingEntity target && !this.level().isClientSide) {
            Entity owner = this.getOwner();
            SpiritusType spiritusType = this.getSpiritusType();
            int spiritusLevel = Math.min(this.getSpiritusLevel(), 4);
            float chargeMultiplier = this.getChargeMultiplier();
            float executeThreshold = this.getExecuteThreshold();

            float targetMaxHealth = target.getMaxHealth();
            float targetCurrentHealth = target.getHealth();

            boolean shouldExecute = executeThreshold > 0
                && targetCurrentHealth <= (targetMaxHealth * executeThreshold)
                && chargeMultiplier >= 1.0f;

            double originalDamage = this.getBaseDamage();

            if (shouldExecute) {
                this.setBaseDamage(targetMaxHealth * 2);

                this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    AnimusSounds.EXECUTE.get(),
                    SoundSource.PLAYERS,
                    1.0f,
                    0.8f // Slightly lower pitch for the bow execute
                );

                if (this.level() instanceof ServerLevel serverLevel) {
                    double x = target.getX();
                    double y = target.getY() + target.getBbHeight() / 2.0;
                    double z = target.getZ();

                    serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        x, y, z,
                        30,
                        0.5, 0.5, 0.5,
                        0.1
                    );
                    serverLevel.sendParticles(
                        ParticleTypes.SOUL,
                        x, y, z,
                        15,
                        0.3, 0.3, 0.3,
                        0.05
                    );
                }
            }

            super.onHitEntity(result);

            // Restore original damage for subsequent pierces after execute
            if (shouldExecute) {
                this.setBaseDamage(originalDamage);
            }

            applySpiritusEffects(target, spiritusType, spiritusLevel, owner instanceof LivingEntity ? (LivingEntity) owner : null, chargeMultiplier);
        } else {
            super.onHitEntity(result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    private void applySpiritusEffects(LivingEntity target, SpiritusType spiritusType, int level,
                                   LivingEntity attacker, float chargeMultiplier) {
        float durationMultiplier = 1.0f + (chargeMultiplier * 0.5f);

        switch (spiritusType) {
            case RUINA:
                target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    (int)(poisonTime[level] * durationMultiplier),
                    poisonLevel[level]
                ));
                if (chargeMultiplier >= 0.5f) {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.WITHER,
                        (int)(witherTime[level] * durationMultiplier),
                        witherLevel[level]
                    ));
                }
                break;

            case INVICTUS:
                target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    (int)(slowTime[level] * durationMultiplier),
                    slowLevel[level]
                ));
                if (chargeMultiplier >= 0.5f) {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.DIG_SLOWDOWN,
                        (int)(slowTime[level] * durationMultiplier),
                        level / 2
                    ));
                }
                if (attacker != null) {
                    attacker.addEffect(new MobEffectInstance(
                        MobEffects.ABSORPTION,
                        (int)(100 * durationMultiplier),
                        Math.min(level / 2, 2)
                    ));
                    if (chargeMultiplier >= 0.75f) {
                        attacker.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            (int)(60 * durationMultiplier),
                            0
                        ));
                    }
                }
                break;

            case VINDICTA:
                target.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    (int)(100 * durationMultiplier),
                    level / 2
                ));
                if (attacker != null) {
                    attacker.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        (int)(100 * durationMultiplier),
                        level / 2
                    ));
                    if (chargeMultiplier >= 0.5f) {
                        attacker.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            (int)(60 * durationMultiplier),
                            0
                        ));
                    }
                }
                break;

            case NIHILUM:
                target.setRemainingFireTicks((int)(60 * durationMultiplier) + level * 20);
                if (chargeMultiplier >= 0.5f) {
                    target.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING,
                        (int)(100 * durationMultiplier),
                        0
                    ));
                }
                break;

            default:
                target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    (int)(60 * durationMultiplier),
                    0
                ));
                break;
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide && this.tickCount % 2 == 0) {
            float charge = this.getChargeMultiplier();

            this.level().addParticle(
                ParticleTypes.FLAME,
                this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                0, 0, 0
            );

            if (charge >= 0.5f && this.tickCount % 4 == 0) {
                this.level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.3,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.3,
                    0, 0, 0
                );
            }

            if (charge >= 1.0f && this.tickCount % 3 == 0) {
                this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1
                );
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("SpiritusType", 8)) {
            this.entityData.set(ID_SPIRITUS_TYPE, tag.getString("SpiritusType"));
        }
        if (tag.contains("SpiritusLevel", 3)) {
            this.entityData.set(ID_SPIRITUS_LEVEL, tag.getInt("SpiritusLevel"));
        }
        if (tag.contains("ChargeMultiplier", 5)) {
            this.entityData.set(ID_CHARGE_MULTIPLIER, tag.getFloat("ChargeMultiplier"));
        }
        if (tag.contains("ExecuteThreshold", 5)) {
            this.entityData.set(ID_EXECUTE_THRESHOLD, tag.getFloat("ExecuteThreshold"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("SpiritusType", this.entityData.get(ID_SPIRITUS_TYPE));
        tag.putInt("SpiritusLevel", this.entityData.get(ID_SPIRITUS_LEVEL));
        tag.putFloat("ChargeMultiplier", this.entityData.get(ID_CHARGE_MULTIPLIER));
        tag.putFloat("ExecuteThreshold", this.entityData.get(ID_EXECUTE_THRESHOLD));
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
