package com.breakinblocks.animusnv.entities;

import com.breakinblocks.animusnv.items.ItemSpear;
import com.breakinblocks.animusnv.items.ItemSpearBound;
import com.breakinblocks.animusnv.items.ItemSpearSentient;
import com.breakinblocks.animusnv.registry.AnimusEntityTypes;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Thrown Spear Entity
 * Custom projectile that deals AOE damage on impact
 */
public class EntityThrownSpear extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(EntityThrownSpear.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> ID_FOIL = SynchedEntityData.defineId(EntityThrownSpear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> ID_VARIANT = SynchedEntityData.defineId(EntityThrownSpear.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> ID_ACTIVATED = SynchedEntityData.defineId(EntityThrownSpear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> ID_SPIRITUS_TYPE = SynchedEntityData.defineId(EntityThrownSpear.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ID_SPIRITUS_LEVEL = SynchedEntityData.defineId(EntityThrownSpear.class, EntityDataSerializers.INT);
    private ItemStack spearItem = ItemStack.EMPTY;
    private boolean dealtDamage;
    public int clientSideReturnTridentTickCount;

    public EntityThrownSpear(EntityType<? extends EntityThrownSpear> entityType, Level level) {
        super(entityType, level);
    }

    public EntityThrownSpear(Level level, LivingEntity shooter, ItemStack stack) {
        super(AnimusEntityTypes.THROWN_PILUM.get(), shooter, level, stack.copy(), stack);
        this.spearItem = stack.copy();

        // Bound and Sentient spears have built-in loyalty (level 3)
        // Other spears use loyalty enchantment level
        int loyalty;
        if (stack.getItem() instanceof ItemSpearBound ||
            stack.getItem() instanceof ItemSpearSentient) {
            loyalty = 3; // Max loyalty level
        } else {
            loyalty = getLoyaltyLevel(level, stack);
        }
        this.entityData.set(ID_LOYALTY, (byte)loyalty);
        this.entityData.set(ID_FOIL, stack.hasFoil());

        String variant = "iron";
        boolean activated = false;
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null) {
            String path = itemId.getPath();
            if (path.contains("diamond")) {
                variant = "diamond";
            } else if (path.contains("bound")) {
                variant = "bound";
                if (stack.getItem() instanceof ItemSpearBound boundSpear) {
                    activated = boundSpear.isActivated(stack);
                }
            }
        }
        this.entityData.set(ID_VARIANT, variant);
        this.entityData.set(ID_ACTIVATED, activated);
    }

    private static int getLoyaltyLevel(Level level, ItemStack stack) {
        if (level.registryAccess() == null) return 0;
        var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var loyaltyHolder = enchantmentRegistry.get(Enchantments.LOYALTY);
        if (loyaltyHolder.isEmpty()) return 0;
        return stack.getEnchantmentLevel(loyaltyHolder.get());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_LOYALTY, (byte)0);
        builder.define(ID_FOIL, false);
        builder.define(ID_VARIANT, "iron");
        builder.define(ID_ACTIVATED, false);
        builder.define(ID_SPIRITUS_TYPE, "DEFAULT");
        builder.define(ID_SPIRITUS_LEVEL, 0);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return this.spearItem == null ? ItemStack.EMPTY : this.spearItem.copy();
    }

    @Override
    public void tick() {
        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        Entity owner = this.getOwner();
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (loyalty > 0 && (this.dealtDamage || this.isNoPhysics()) && owner != null) {
            if (!this.isAcceptibleReturnOwner()) {
                if (this.level() instanceof ServerLevel serverLevel && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(serverLevel, this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 vec3 = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015 * (double)loyalty, this.getZ());
                if (this.level().isClientSide()) {
                    this.yOld = this.getY();
                }

                double d0 = 0.05 * (double)loyalty;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95).add(vec3.normalize().scale(d0)));
                if (this.clientSideReturnTridentTickCount == 0) {
                    this.playSound(SoundEvents.TRIDENT_RETURN, 10.0F, 1.0F);
                }

                ++this.clientSideReturnTridentTickCount;
            }
        }

        super.tick();
    }

    private boolean isAcceptibleReturnOwner() {
        Entity entity = this.getOwner();
        if (entity != null && entity.isAlive()) {
            return !(entity instanceof Player player && player.isSpectator());
        } else {
            return false;
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.spearItem.copy();
    }

    public ItemStack getSpearItem() {
        return this.spearItem;
    }

    public boolean isFoil() {
        return this.entityData.get(ID_FOIL);
    }

    public String getVariant() {
        return this.entityData.get(ID_VARIANT);
    }

    public void setVariant(String variant) {
        this.entityData.set(ID_VARIANT, variant);
    }

    public void setSpiritusType(SpiritusType type) {
        this.entityData.set(ID_SPIRITUS_TYPE, type.toString());
    }

    public SpiritusType getSpiritusType() {
        try {
            return SpiritusType.valueOf(this.entityData.get(ID_SPIRITUS_TYPE));
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
    protected void onHit(HitResult result) {
        super.onHit(result);

        boolean isBound = "bound".equals(this.getVariant());
        boolean isActivated = this.entityData.get(ID_ACTIVATED);
        if (isBound && isActivated && !this.level().isClientSide()) {
            Entity owner = this.getOwner();
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level(), EntitySpawnReason.TRIGGERED);
            if (lightning != null) {
                lightning.snapTo(result.getLocation().x, result.getLocation().y, result.getLocation().z);
                lightning.setCause(owner instanceof ServerPlayer ? (ServerPlayer)owner : null);
                lightning.setVisualOnly(true);
                this.level().addFreshEntity(lightning);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        float damage = 8.0F;

        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
        this.dealtDamage = true;

        if (entity.hurtOrSimulate(damageSource, damage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity livingEntity) {
                this.doPostHurtEffects(livingEntity);
            }
        }

        boolean isBound = "bound".equals(this.getVariant());
        boolean isActivated = this.entityData.get(ID_ACTIVATED);
        float aoeDamage = (isBound && isActivated) ? damage * 1.0F : damage * 0.75F;
        dealAOEDamage(entity.getX(), entity.getY(), entity.getZ(), aoeDamage);

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    private void dealAOEDamage(double x, double y, double z, float damage) {
        if (this.level().isClientSide()) {
            return;
        }

        int range = 5;
        AABB region = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, region);

        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);

        boolean isSentient = "sentient".equals(this.getVariant());
        SpiritusType spiritusType = isSentient ? this.getSpiritusType() : null;
        int spiritusLevel = isSentient ? this.getSpiritusLevel() : 0;

        for (LivingEntity target : entities) {
            if (target == null || target.isDeadOrDying() || target == owner) {
                continue;
            }

            // Skip direct hit target (already took damage)
            double dist = target.distanceToSqr(x, y, z);
            if (dist < 1.0) {
                continue;
            }

            target.hurt(damageSource, damage);

            if (isSentient && spiritusType != null && owner instanceof LivingEntity livingOwner) {
                ItemSpearSentient.applyEffectToEntity(spiritusType, spiritusLevel, target, livingOwner);
            }
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || this.isNoPhysics() && this.ownedBy(player) && player.getInventory().add(this.getPickupItem());
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (this.ownedBy(player) || this.getOwner() == null) {
            super.playerTouch(player);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("Spear", ItemStack.CODEC).ifPresent(stack -> this.spearItem = stack);
        this.dealtDamage = input.getBooleanOr("DealtDamage", false);

        // Bound and Sentient spears have built-in loyalty (level 3)
        // Other spears use loyalty enchantment level
        int loyalty;
        if (this.spearItem.getItem() instanceof ItemSpearBound ||
            this.spearItem.getItem() instanceof ItemSpearSentient) {
            loyalty = 3; // Max loyalty level
        } else {
            loyalty = getLoyaltyLevel(this.level(), this.spearItem);
        }
        this.entityData.set(ID_LOYALTY, (byte)loyalty);

        this.entityData.set(ID_VARIANT, input.getStringOr("Variant", this.entityData.get(ID_VARIANT)));
        this.entityData.set(ID_ACTIVATED, input.getBooleanOr("Activated", this.entityData.get(ID_ACTIVATED)));
        this.entityData.set(ID_SPIRITUS_TYPE, input.getStringOr("SpiritusType", this.entityData.get(ID_SPIRITUS_TYPE)));
        this.entityData.set(ID_SPIRITUS_LEVEL, input.getIntOr("SpiritusLevel", this.entityData.get(ID_SPIRITUS_LEVEL)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Spear", ItemStack.CODEC, this.spearItem);
        output.putBoolean("DealtDamage", this.dealtDamage);
        output.putString("Variant", this.getVariant());
        output.putBoolean("Activated", this.entityData.get(ID_ACTIVATED));
        output.putString("SpiritusType", this.entityData.get(ID_SPIRITUS_TYPE));
        output.putInt("SpiritusLevel", this.entityData.get(ID_SPIRITUS_LEVEL));
    }

    @Override
    public void tickDespawn() {
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (this.pickup != AbstractArrow.Pickup.ALLOWED || loyalty <= 0) {
            super.tickDespawn();
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
