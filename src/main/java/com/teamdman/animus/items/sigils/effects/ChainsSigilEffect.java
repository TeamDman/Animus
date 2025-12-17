package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusDataComponents;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;

/**
 * Sigil of Chains - captures entities into soul items.
 * Uses raycasting to find target entity within range.
 */
public record ChainsSigilEffect() implements ISigilEffect {
    public static final MapCodec<ChainsSigilEffect> CODEC = MapCodec.unit(ChainsSigilEffect::new);
    private static final double CAPTURE_RANGE = 5.0;

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return false;
        }

        // Raycast to find entity player is looking at
        LivingEntity target = getTargetEntity(player);
        if (target == null) {
            return false;
        }

        return captureEntity(player, target);
    }

    @Override
    public boolean useOnEntity(Level level, Player player, ItemStack stack, Entity target) {
        if (level.isClientSide || !(target instanceof LivingEntity living)) {
            return false;
        }

        return captureEntity(player, living);
    }

    /**
     * Get the living entity the player is looking at within capture range.
     */
    private LivingEntity getTargetEntity(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 reachVec = eyePos.add(lookVec.scale(CAPTURE_RANGE));

        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(CAPTURE_RANGE)).inflate(1.0);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player,
                eyePos,
                reachVec,
                searchBox,
                entity -> entity instanceof LivingEntity && entity != player && entity.isAlive(),
                CAPTURE_RANGE * CAPTURE_RANGE
        );

        if (hitResult != null && hitResult.getEntity() instanceof LivingEntity living) {
            return living;
        }

        return null;
    }

    /**
     * Attempt to capture the target entity.
     * @return true if capture was successful
     */
    private boolean captureEntity(Player player, LivingEntity target) {
        // Check if entity can be captured
        if (target.getType().is(Constants.Tags.DISALLOW_CAPTURING)) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.CHAINS_CAPTURE_FAILED),
                    true
            );
            return false;
        }

        // Create mob soul item
        ItemStack soul = new ItemStack(AnimusItems.MOBSOUL.get());
        CompoundTag targetData = new CompoundTag();

        // Save entity data
        target.saveWithoutId(targetData);

        // Get entity type
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
        if (entityId != null) {
            soul.set(AnimusDataComponents.SOUL_ENTITY_NAME.get(), entityId.toString());
        }

        // Save custom name if present
        String customName = null;
        if (target instanceof Mob && target.hasCustomName()) {
            customName = target.getCustomName().getString();
            soul.set(AnimusDataComponents.SOUL_NAME.get(), customName);
        }

        soul.set(AnimusDataComponents.SOUL_DATA.get(), targetData);

        // Set display name
        String displayName = customName != null
                ? customName
                : target.getType().getDescription().getString() + " Soul";
        soul.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(displayName));

        // Give item to player or drop it
        if (!player.getInventory().add(soul)) {
            ItemEntity itemEntity = new ItemEntity(
                    player.level(),
                    target.getX(),
                    target.getY(),
                    target.getZ(),
                    soul
            );
            player.level().addFreshEntity(itemEntity);
        }

        // Remove the captured entity
        target.discard();

        return true;
    }
}
