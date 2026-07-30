package com.breakinblocks.animusnv.entities;

import com.breakinblocks.animusnv.items.ItemSentientBow;
import com.breakinblocks.animusnv.util.SpiritusWeaponStats;
import com.breakinblocks.animusnv.registry.AnimusEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private static final EntityDataAccessor<String> ID_SPIRITUS_TYPE =
        SynchedEntityData.defineId(EntitySentientArrow.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ID_SPIRITUS_LEVEL =
        SynchedEntityData.defineId(EntitySentientArrow.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> ID_BONUS_DAMAGE =
        SynchedEntityData.defineId(EntitySentientArrow.class, EntityDataSerializers.FLOAT);

    private static final int[] poisonTime = ItemSentientBow.poisonTime;
    private static final int[] poisonLevel = SpiritusWeaponStats.POISON_LEVEL;
    private static final int[] slowTime = ItemSentientBow.slowTime;
    private static final int[] slowLevel = ItemSentientBow.slowLevel;

    private double baseDamage = 2.0;

    public EntitySentientArrow(EntityType<? extends EntitySentientArrow> entityType, Level level) {
        super(entityType, level);
        this.pickup = Pickup.DISALLOWED;
    }

    public EntitySentientArrow(Level level, LivingEntity shooter) {
        super(AnimusEntityTypes.SENTIENT_ARROW.get(), shooter, level, ItemStack.EMPTY, null);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SPIRITUS_TYPE, SpiritusType.RAW.toString());
        builder.define(ID_SPIRITUS_LEVEL, 0);
        builder.define(ID_BONUS_DAMAGE, 0.0F);
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

    @Override
    public void setBaseDamage(double baseDamage) {
        super.setBaseDamage(baseDamage);
        this.baseDamage = baseDamage;
    }

    public double getBaseDamage() {
        return this.baseDamage;
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

        if (entity instanceof LivingEntity target && !this.level().isClientSide()) {
            Entity owner = this.getOwner();
            SpiritusType spiritusType = this.getSpiritusType();
            int spiritusLevel = Math.min(this.getSpiritusLevel(), 4);

            // Spiritus drops handled by AnimusEventHandler.handleSentientWeaponSpiritusDrops()
            applySpiritusEffects(target, spiritusType, spiritusLevel, owner instanceof LivingEntity ? (LivingEntity) owner : null);
        }

        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    private void applySpiritusEffects(LivingEntity target, SpiritusType spiritusType, int level, LivingEntity attacker) {
        switch (spiritusType) {
            case RUINA:
                target.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    poisonTime[level],
                    poisonLevel[level]
                ));
                break;

            case INVICTUS:
                target.addEffect(new MobEffectInstance(
                    MobEffects.SLOWNESS,
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
                        MobEffects.SPEED,
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

        if (this.level().isClientSide() && this.tickCount % 2 == 0) {
            this.level().addParticle(
                ParticleTypes.END_ROD,
                this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                0, 0, 0
            );
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.baseDamage = input.getDoubleOr("damage", 2.0);
        this.entityData.set(ID_SPIRITUS_TYPE, input.getStringOr("SpiritusType", this.entityData.get(ID_SPIRITUS_TYPE)));
        this.entityData.set(ID_SPIRITUS_LEVEL, input.getIntOr("SpiritusLevel", this.entityData.get(ID_SPIRITUS_LEVEL)));
        this.entityData.set(ID_BONUS_DAMAGE, input.getFloatOr("BonusDamage", this.entityData.get(ID_BONUS_DAMAGE)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("SpiritusType", this.entityData.get(ID_SPIRITUS_TYPE));
        output.putInt("SpiritusLevel", this.entityData.get(ID_SPIRITUS_LEVEL));
        output.putFloat("BonusDamage", this.entityData.get(ID_BONUS_DAMAGE));
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
