package com.breakinblocks.animusnv.entities;

import com.breakinblocks.animusnv.items.ItemSentientBow;
import com.breakinblocks.animusnv.util.WillWeaponStats;
import com.breakinblocks.animusnv.registry.AnimusEntityTypes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
/**
 * Sentient Arrow Entity
 * Virtual arrow fired by the Sentient Bow
 * - Vanishes after impact (no item left behind)
 * - Drops Spiritus matching the bow's attuned type on kill
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

    private static final int[] poisonTime = ItemSentientBow.poisonTime;
    private static final int[] poisonLevel = WillWeaponStats.POISON_LEVEL;
    private static final int[] slowTime = ItemSentientBow.slowTime;
    private static final int[] slowLevel = ItemSentientBow.slowLevel;

    public EntitySentientArrow(EntityType<? extends EntitySentientArrow> entityType, Level level) {
        super(entityType, level);
        this.pickup = Pickup.DISALLOWED;
    }

    public EntitySentientArrow(Level level, LivingEntity shooter) {
        super(AnimusEntityTypes.SENTIENT_ARROW.get(), shooter, level, ItemStack.EMPTY, ItemStack.EMPTY);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_WILL_TYPE, SpiritusType.RAW.toString());
        builder.define(ID_WILL_LEVEL, 0);
        builder.define(ID_BONUS_DAMAGE, 0.0F);
    }

    public void setWillType(SpiritusType type) {
        this.entityData.set(ID_WILL_TYPE, type.toString());
    }

    public SpiritusType getWillType() {
        try {
            return SpiritusType.valueOf(this.entityData.get(ID_WILL_TYPE).toUpperCase());
        } catch (IllegalArgumentException e) {
            return SpiritusType.RAW;
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

        float baseDamage = (float) this.getBaseDamage();
        float bonusDamage = this.getBonusDamage();
        float totalDamage = baseDamage + bonusDamage;

        this.setBaseDamage(totalDamage);
        super.onHitEntity(result);

        if (entity instanceof LivingEntity target && !this.level().isClientSide) {
            Entity owner = this.getOwner();
            SpiritusType willType = this.getWillType();
            int willLevel = Math.min(this.getWillLevel(), 4);

            // Will drops handled by AnimusEventHandler.handleSentientWeaponWillDrops()
            applyWillEffects(target, willType, willLevel, owner instanceof LivingEntity ? (LivingEntity) owner : null);
        }

        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    private void applyWillEffects(LivingEntity target, SpiritusType willType, int level, LivingEntity attacker) {
        switch (willType) {
            case RUINA:
                target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    poisonTime[level],
                    poisonLevel[level]
                ));
                break;

            case INVICTUS:
                target.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    slowTime[level],
                    slowLevel[level]
                ));
                if (attacker != null) {
                    attacker.addEffect(new MobEffectInstance(
                        MobEffects.ABSORPTION,
                        100,
                        0
                    ));
                }
                break;

            case VINDICTA:
                if (attacker != null) {
                    attacker.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        100,
                        level / 2
                    ));
                }
                break;

            case NIHILUM:
                break;

            default:
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
            this.level().addParticle(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                0, 0, 0
            );
        }
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
