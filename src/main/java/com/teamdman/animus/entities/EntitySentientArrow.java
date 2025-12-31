package com.teamdman.animus.entities;

import com.teamdman.animus.items.ItemSentientBow;
import com.teamdman.animus.registry.AnimusEntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
/**
 * Sentient Arrow Entity
 * Virtual arrow fired by the Sentient Bow
 * - Vanishes after impact (no item left behind)
 * - Drops demon will matching the bow's attuned type on kill
 * - Deals bonus damage based on will type and level
 * - Applies status effects based on will type
 */
public class EntitySentientArrow extends AbstractArrow {
    private static final EntityDataAccessor<String> ID_WILL_TYPE =
        SynchedEntityData.defineId(EntitySentientArrow.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ID_WILL_LEVEL =
        SynchedEntityData.defineId(EntitySentientArrow.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ID_BONUS_DAMAGE =
        SynchedEntityData.defineId(EntitySentientArrow.class, EntityDataSerializers.FLOAT);

    // Effect parameters from ItemSentientBow
    private static final int[] poisonTime = ItemSentientBow.poisonTime;
    private static final int[] poisonLevel = ItemSentientBow.poisonLevel;
    private static final int[] slowTime = ItemSentientBow.slowTime;
    private static final int[] slowLevel = ItemSentientBow.slowLevel;

    public EntitySentientArrow(EntityType<? extends EntitySentientArrow> entityType, Level level) {
        super(entityType, level);
        this.pickup = Pickup.DISALLOWED;
    }

    public EntitySentientArrow(Level level, LivingEntity shooter) {
        super(AnimusEntityTypes.SENTIENT_ARROW.get(), shooter, level);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_WILL_TYPE, EnumDemonWillType.DEFAULT.toString());
        this.entityData.define(ID_WILL_LEVEL, 0);
        this.entityData.define(ID_BONUS_DAMAGE, 0.0F);
    }

    public void setWillType(EnumDemonWillType type) {
        this.entityData.set(ID_WILL_TYPE, type.toString());
    }

    public EnumDemonWillType getWillType() {
        try {
            return EnumDemonWillType.valueOf(this.entityData.get(ID_WILL_TYPE).toUpperCase());
        } catch (IllegalArgumentException e) {
            return EnumDemonWillType.DEFAULT;
        }
    }

    public void setWillLevel(int level) {
        this.entityData.set(ID_WILL_LEVEL, level);
    }

    public int getWillLevel() {
        return this.entityData.get(ID_WILL_LEVEL);
    }

    public void setBonusDamage(double damage) {
        this.entityData.set(ID_BONUS_DAMAGE, (float) damage);
    }

    public float getBonusDamage() {
        return this.entityData.get(ID_BONUS_DAMAGE);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();

        // Calculate total damage (base arrow damage + bonus from will)
        float baseDamage = (float) this.getBaseDamage();
        float bonusDamage = this.getBonusDamage();
        float totalDamage = baseDamage + bonusDamage;

        // Set the damage for the parent class to use
        this.setBaseDamage(totalDamage);

        // Let the parent handle the actual damage
        super.onHitEntity(result);

        // Apply effects only if the target is living
        if (entity instanceof LivingEntity target && !this.level().isClientSide) {
            Entity owner = this.getOwner();
            EnumDemonWillType willType = this.getWillType();
            int willLevel = Math.min(this.getWillLevel(), 4);

            // Will drops are handled by AnimusEventHandler.handleSentientWeaponWillDrops()

            // Apply effects based on will type
            applyWillEffects(target, willType, willLevel, owner instanceof LivingEntity ? (LivingEntity) owner : null);
        }

        // Vanish after hitting an entity
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        // Vanish after hitting a block
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    /**
     * Apply status effects based on will type
     */
    private void applyWillEffects(LivingEntity target, EnumDemonWillType willType, int level, LivingEntity attacker) {
        switch (willType) {
            case CORROSIVE:
                // Poison effect
                target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    poisonTime[level],
                    poisonLevel[level]
                ));
                break;

            case STEADFAST:
                // Slowness effect
                target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slowTime[level],
                    slowLevel[level]
                ));
                // Give attacker absorption if they hit
                if (attacker != null) {
                    attacker.addEffect(new MobEffectInstance(
                        MobEffects.ABSORPTION,
                        100, // 5 seconds
                        0
                    ));
                }
                break;

            case VENGEFUL:
                // Speed boost to attacker
                if (attacker != null) {
                    attacker.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        100, // 5 seconds
                        level / 2
                    ));
                }
                break;

            case DESTRUCTIVE:
                // Extra knockback (handled by base damage being higher)
                break;

            default:
                // Raw will - no special effects
                break;
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        // Return empty since this arrow can't be picked up
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();

        // Add spectral particles for visual effect
        if (this.level().isClientSide && this.tickCount % 2 == 0) {
            EnumDemonWillType willType = this.getWillType();
            double[] color = getWillColor(willType);

            this.level().addParticle(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                0, 0, 0
            );
        }
    }

    /**
     * Get RGB color values for will type (for potential particle effects)
     */
    private double[] getWillColor(EnumDemonWillType type) {
        return switch (type) {
            case CORROSIVE -> new double[]{0.0, 0.8, 0.0}; // Green
            case DESTRUCTIVE -> new double[]{0.8, 0.0, 0.0}; // Red
            case VENGEFUL -> new double[]{0.8, 0.0, 0.8}; // Purple
            case STEADFAST -> new double[]{0.8, 0.8, 0.0}; // Yellow
            default -> new double[]{0.5, 0.5, 0.5}; // Gray for raw
        };
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("WillType", 8)) {
            this.entityData.set(ID_WILL_TYPE, tag.getString("WillType"));
        }
        if (tag.contains("WillLevel", 3)) {
            this.entityData.set(ID_WILL_LEVEL, tag.getInt("WillLevel"));
        }
        if (tag.contains("BonusDamage", 5)) {
            this.entityData.set(ID_BONUS_DAMAGE, tag.getFloat("BonusDamage"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("WillType", this.entityData.get(ID_WILL_TYPE));
        tag.putInt("WillLevel", this.entityData.get(ID_WILL_LEVEL));
        tag.putFloat("BonusDamage", this.entityData.get(ID_BONUS_DAMAGE));
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
