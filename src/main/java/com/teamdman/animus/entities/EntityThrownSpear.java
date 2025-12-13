package com.teamdman.animus.entities;

import com.teamdman.animus.items.ItemSpear;
import com.teamdman.animus.items.ItemSpearSentient;
import com.teamdman.animus.registry.AnimusEntityTypes;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.will.WorldDemonWillHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
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
    private static final EntityDataAccessor<String> ID_WILL_TYPE = SynchedEntityData.defineId(EntityThrownSpear.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ID_WILL_LEVEL = SynchedEntityData.defineId(EntityThrownSpear.class, EntityDataSerializers.INT);
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
        if (stack.getItem() instanceof com.teamdman.animus.items.ItemSpearBound ||
            stack.getItem() instanceof com.teamdman.animus.items.ItemSpearSentient) {
            loyalty = 3; // Max loyalty level
        } else {
            loyalty = getLoyaltyLevel(level, stack);
        }
        this.entityData.set(ID_LOYALTY, (byte)loyalty);
        this.entityData.set(ID_FOIL, stack.hasFoil());

        // Determine variant from item registry name and check activation state
        String variant = "iron"; // default
        boolean activated = false;
        net.minecraft.resources.ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId != null) {
            String path = itemId.getPath();
            if (path.contains("diamond")) {
                variant = "diamond";
            } else if (path.contains("bound")) {
                variant = "bound";
                // Check activation state from NBT
                if (stack.getItem() instanceof com.teamdman.animus.items.ItemSpearBound boundSpear) {
                    activated = boundSpear.isActivated(stack);
                }
            }
        }
        this.entityData.set(ID_VARIANT, variant);
        this.entityData.set(ID_ACTIVATED, activated);
    }

    // Helper method for getting loyalty in 1.21.1
    private static int getLoyaltyLevel(Level level, ItemStack stack) {
        if (level.registryAccess() == null) return 0;
        var enchantmentRegistry = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        var loyaltyHolder = enchantmentRegistry.getHolder(Enchantments.LOYALTY);
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
        builder.define(ID_WILL_TYPE, "DEFAULT");
        builder.define(ID_WILL_LEVEL, 0);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return this.spearItem.copy();
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
                if (!this.level().isClientSide && this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoPhysics(true);
                Vec3 vec3 = owner.getEyePosition().subtract(this.position());
                this.setPosRaw(this.getX(), this.getY() + vec3.y * 0.015 * (double)loyalty, this.getZ());
                if (this.level().isClientSide) {
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
            return !(entity instanceof net.minecraft.world.entity.player.Player player && player.isSpectator());
        } else {
            return false;
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        return this.spearItem.copy();
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

    public void setWillType(EnumWillType type) {
        this.entityData.set(ID_WILL_TYPE, type.toString());
    }

    public EnumWillType getWillType() {
        try {
            return EnumWillType.valueOf(this.entityData.get(ID_WILL_TYPE));
        } catch (IllegalArgumentException e) {
            return EnumWillType.DEFAULT;
        }
    }

    public void setWillLevel(int level) {
        this.entityData.set(ID_WILL_LEVEL, level);
    }

    public int getWillLevel() {
        return this.entityData.get(ID_WILL_LEVEL);
    }

    @Override
    protected void onHit(net.minecraft.world.phys.HitResult result) {
        super.onHit(result);

        // Bound spear creates visual-only lightning at any impact point (only when activated)
        // Visual-only means no fire, no thunder sound, just the lightning effect
        boolean isBound = "bound".equals(this.getVariant());
        boolean isActivated = this.entityData.get(ID_ACTIVATED);
        if (isBound && isActivated && !this.level().isClientSide) {
            Entity owner = this.getOwner();
            net.minecraft.world.entity.LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level());
            if (lightning != null) {
                lightning.moveTo(result.getLocation().x, result.getLocation().y, result.getLocation().z);
                lightning.setCause(owner instanceof net.minecraft.server.level.ServerPlayer ? (net.minecraft.server.level.ServerPlayer)owner : null);
                lightning.setVisualOnly(true); // Visual only - no fire, no thunder
                this.level().addFreshEntity(lightning);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        float damage = 8.0F;
        // In 1.21.1, getMobType() is removed - enchantment damage is handled by damage system

        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);
        this.dealtDamage = true;

        if (entity.hurt(damageSource, damage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity livingEntity) {
                // In 1.21.1, enchantment post-hurt effects handled by damage system
                this.doPostHurtEffects(livingEntity);
            }
        }

        // AOE damage on impact
        // Activated bound spear deals more AOE damage (lightning is handled in onHit)
        boolean isBound = "bound".equals(this.getVariant());
        boolean isActivated = this.entityData.get(ID_ACTIVATED);
        float aoeDamage = (isBound && isActivated) ? damage * 1.0F : damage * 0.75F;
        dealAOEDamage(entity.getX(), entity.getY(), entity.getZ(), aoeDamage);

        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    /**
     * Deal AOE damage around impact point
     */
    private void dealAOEDamage(double x, double y, double z, float damage) {
        if (this.level().isClientSide) {
            return;
        }

        int range = 5;
        AABB region = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, region);

        Entity owner = this.getOwner();
        DamageSource damageSource = this.damageSources().trident(this, owner == null ? this : owner);

        boolean isSentient = "sentient".equals(this.getVariant());
        EnumWillType willType = isSentient ? this.getWillType() : null;
        int willLevel = isSentient ? this.getWillLevel() : 0;

        for (LivingEntity target : entities) {
            if (target == null || target.isDeadOrDying() || target == owner) {
                continue;
            }

            // Skip if entity is already the direct hit target (already took damage)
            double dist = target.distanceToSqr(x, y, z);
            if (dist < 1.0) {
                continue;
            }

            target.hurt(damageSource, damage);

            // Apply sentient effects to AOE targets
            if (isSentient && willType != null && owner instanceof LivingEntity livingOwner) {
                ItemSpearSentient.applyEffectToEntity(willType, willLevel, target, livingOwner);
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
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Spear", 10)) {
            this.spearItem = ItemStack.parseOptional(this.level().registryAccess(), tag.getCompound("Spear"));
        }
        this.dealtDamage = tag.getBoolean("DealtDamage");

        // Bound and Sentient spears have built-in loyalty (level 3)
        // Other spears use loyalty enchantment level
        int loyalty;
        if (this.spearItem.getItem() instanceof com.teamdman.animus.items.ItemSpearBound ||
            this.spearItem.getItem() instanceof com.teamdman.animus.items.ItemSpearSentient) {
            loyalty = 3; // Max loyalty level
        } else {
            loyalty = getLoyaltyLevel(this.level(), this.spearItem);
        }
        this.entityData.set(ID_LOYALTY, (byte)loyalty);

        if (tag.contains("Variant", 8)) {
            this.entityData.set(ID_VARIANT, tag.getString("Variant"));
        }
        if (tag.contains("Activated", 1)) {
            this.entityData.set(ID_ACTIVATED, tag.getBoolean("Activated"));
        }
        if (tag.contains("WillType", 8)) {
            this.entityData.set(ID_WILL_TYPE, tag.getString("WillType"));
        }
        if (tag.contains("WillLevel", 3)) {
            this.entityData.set(ID_WILL_LEVEL, tag.getInt("WillLevel"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Spear", this.spearItem.save(this.level().registryAccess()));
        tag.putBoolean("DealtDamage", this.dealtDamage);
        tag.putString("Variant", this.getVariant());
        tag.putBoolean("Activated", this.entityData.get(ID_ACTIVATED));
        tag.putString("WillType", this.entityData.get(ID_WILL_TYPE));
        tag.putInt("WillLevel", this.entityData.get(ID_WILL_LEVEL));
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
