package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.util.SoulTicket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Sigil of Heavenly Wrath - Levitates enemies then drops them for fall damage
 * <p>
 * Features:
 * - Levitates all enemies in 16 block radius (not the user)
 * - Applies upward velocity boost for 100% faster levitation
 * - After 3 seconds, removes levitation and applies downward velocity
 * - Flying entities get stronger downward velocity based on height from ground
 * <p>
 * Tick handler hooked up in AnimusEventHandler.onLevelTick()
 */
public class ItemSigilHeavenlyWrath extends AnimusSigilBase {

    // Track pending fall effects - list of (level, entityUUID, playerUUID, tickToFall, heightFromGround)
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

    public ItemSigilHeavenlyWrath() {
        super(Constants.Sigils.HEAVENLY_WRATH, 1000);
    }

    /**
     * Process pending fall effects - should be called from a tick event
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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Check binding
        var binding = getBinding(stack);
        if (binding == null || binding.isEmpty() || !binding.uuid().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

        // Consume LP from soul network
        wayoftime.bloodmagic.common.datacomponent.SoulNetwork network = wayoftime.bloodmagic.util.helper.SoulNetworkHelper.getSoulNetwork(player);
        SoulTicket ticket = SoulTicket.create(getLpUsed());

        var syphonResult = network.syphonAndDamage(player, ticket);
        if (!syphonResult.isSuccess()) {
            return InteractionResultHolder.fail(stack);
        }

        if (level instanceof ServerLevel serverLevel) {
            // Get all living entities in a 16 block radius
            Vec3 playerPos = player.position();
            AABB area = AABB.ofSize(playerPos, 32.0, 32.0, 32.0); // 16 block radius = 32 block box
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

            long currentTick = serverLevel.getServer().getTickCount();
            long fallTick = currentTick + 60; // 3 seconds (60 ticks)

            for (LivingEntity entity : entities) {
                // Skip the player who used the sigil
                if (entity == player) {
                    continue;
                }

                // Apply levitation effect for 3 seconds
                entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1));

                // Apply upward velocity boost for 100% faster levitation
                Vec3 currentVelocity = entity.getDeltaMovement();
                entity.setDeltaMovement(currentVelocity.x, currentVelocity.y + 0.8, currentVelocity.z);
                entity.hurtMarked = true; // Sync to client

                // Calculate height from ground for stronger fall effect on flying entities
                double heightFromGround = calculateHeightFromGround(entity);

                // Schedule the fall effect
                pendingFalls.add(new PendingFall(
                    serverLevel,
                    entity.getUUID(),
                    player.getUUID(),
                    fallTick,
                    heightFromGround
                ));
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }

    /**
     * Calculate how far an entity is from the ground
     */
    private double calculateHeightFromGround(LivingEntity entity) {
        double entityY = entity.getY();
        double groundY = entityY;

        // Search downward for the first solid block
        Level level = entity.level();
        for (int y = (int) entityY; y >= level.getMinBuildHeight(); y--) {
            if (level.getBlockState(entity.blockPosition().atY(y)).isSolid()) {
                groundY = y + 1; // +1 because we want to be standing on top of the block
                break;
            }
        }

        return entityY - groundY;
    }

    /**
     * Apply the fall effect to an entity
     * Removes levitation, applies downward velocity, and applies heavy_heart to prevent flight
     */
    private static void applyFallEffect(LivingEntity entity, double heightFromGround) {
        // Remove levitation
        entity.removeEffect(MobEffects.LEVITATION);

        // Apply Blood Magic's heavy_heart effect to prevent flight for 2 seconds at amplifier 4
        ResourceLocation heavyHeartRL = ResourceLocation.fromNamespaceAndPath("bloodmagic", "heavy_heart");
        var heavyHeartOpt = BuiltInRegistries.MOB_EFFECT.getOptional(heavyHeartRL);
        if (heavyHeartOpt.isPresent()) {
            entity.addEffect(new MobEffectInstance(Holder.direct(heavyHeartOpt.get()), 40, 4)); // 40 ticks = 2 seconds, amplifier 4
        }

        // Base downward velocity
        double downwardVelocity = -1.5;

        // If entity was high up (flying or levitated high), increase the downward velocity
        // Scale based on height: every 5 blocks adds -0.5 to velocity
        if (heightFromGround > 5.0) {
            double additionalVelocity = Math.min((heightFromGround / 5.0) * -0.5, -3.0); // Cap at -3.0
            downwardVelocity += additionalVelocity;
        }

        // Apply the downward velocity
        Vec3 currentVelocity = entity.getDeltaMovement();
        entity.setDeltaMovement(currentVelocity.x * 0.5, downwardVelocity, currentVelocity.z * 0.5);

        // Set velocity changed flag so it syncs to client
        entity.hurtMarked = true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.translatable(Constants.Localizations.Tooltips.SIGIL_HEAVENLY_WRATH_FLAVOUR));
        tooltip.add(net.minecraft.network.chat.Component.translatable(Constants.Localizations.Tooltips.SIGIL_HEAVENLY_WRATH_INFO));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
