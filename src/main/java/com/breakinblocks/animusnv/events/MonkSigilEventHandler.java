package com.breakinblocks.animusnv.events;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.items.sigils.effects.MonkSigilEffect;
import com.breakinblocks.animusnv.registry.AnimusAttributes;
import com.breakinblocks.animusnv.registry.AnimusItems;
import com.breakinblocks.animusnv.registry.AnimusSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.animusnv.util.InventorySearchHelper;
import javax.annotation.Nullable;
import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import com.breakinblocks.neovitae.api.spiritus.IPlayerSpiritusHandler;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class MonkSigilEventHandler {

    private static final double MIN_SPIRITUS_FOR_EXECUTE = 1.0;
    private static final double MAX_SPIRITUS_FOR_EXECUTE = 4096.0;
    private static final double MIN_EXECUTE_PERCENT = 0.01; // 1%
    private static final double MAX_EXECUTE_PERCENT = 0.15; // 15%
    private static final double SPIRITUS_COST_PER_EXECUTE = 5.0;
    private static final int EV_REWARD_PER_EXECUTE = 200;
    private static final int EV_COST_PER_ACTION = 5;

    // Netherite pickaxe base (9.0) + Efficiency V (level^2 + 1 = 26) = 35.0
    private static final float MONK_MINING_SPEED = 35.0f;

    public static boolean hasActiveMonkSigil(Player player) {
        return findActiveMonkSigil(player) != null;
    }

    @Nullable
    public static ItemStack findActiveMonkSigil(Player player) {
        return InventorySearchHelper
            .findActiveSigil(player, AnimusItems.SIGIL_MONK.get())
            .orElse(null);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        // Must be direct melee, not a projectile/spell (prevents Iron's Spells triggering this)
        if (event.getSource().getDirectEntity() != player) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        ItemStack monkSigil = findActiveMonkSigil(player);
        if (monkSigil == null) {
            return;
        }

        double unarmedDamage = player.getAttributeValue(AnimusAttributes.UNARMED_DAMAGE);
        if (unarmedDamage <= 0) {
            return;
        }

        LivingEntity target = event.getEntity();
        float targetMaxHealth = target.getMaxHealth();
        float baseDamage = event.getAmount() + (float) unarmedDamage;

        double totalSpiritus = getTotalSpiritus(player);

        double executePercent = 0;
        float spiritusBonusDamage = 0;

        if (totalSpiritus >= MIN_SPIRITUS_FOR_EXECUTE) {
            double clampedSpiritus = Math.min(totalSpiritus, MAX_SPIRITUS_FOR_EXECUTE);
            double spiritusRatio = (clampedSpiritus - MIN_SPIRITUS_FOR_EXECUTE) / (MAX_SPIRITUS_FOR_EXECUTE - MIN_SPIRITUS_FOR_EXECUTE);
            executePercent = MIN_EXECUTE_PERCENT + spiritusRatio * (MAX_EXECUTE_PERCENT - MIN_EXECUTE_PERCENT);
            spiritusBonusDamage = (float) (targetMaxHealth * executePercent);
        }

        float totalDamage = baseDamage + spiritusBonusDamage;
        event.setAmount(totalDamage);

        float healthAfterAttack = target.getHealth() - totalDamage;
        float executeThreshold = (float) (targetMaxHealth * executePercent);

        MonkSigilEffect.consumeEV(player, monkSigil, EV_COST_PER_ACTION);

        if (executePercent > 0 && healthAfterAttack > 0 && healthAfterAttack < executeThreshold) {
            if (totalSpiritus >= SPIRITUS_COST_PER_EXECUTE) {
                consumeSpiritus(player, SPIRITUS_COST_PER_EXECUTE);

                event.setAmount(targetMaxHealth * 2);
                player.level().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    AnimusSounds.EXECUTE.get(),
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f
                );

                if (player.level() instanceof ServerLevel serverLevel) {
                    double x = target.getX();
                    double y = target.getY() + target.getBbHeight() / 2.0;
                    double z = target.getZ();
                    serverLevel.sendParticles(
                        ParticleTypes.SOUL,
                        x, y, z,
                        20,
                        0.5, 0.5, 0.5,
                        0.1
                    );
                }

                // Reward EV to the sigil's bound network
                IAnima network = AnimusRitualHelper.getNetworkForBoundItem(player, monkSigil);
                if (network != null) {
                    network.add(AnimaTicket.create(EV_REWARD_PER_EXECUTE), EV_REWARD_PER_EXECUTE);
                }
            }
        }

        double knockbackStrength = 0.5D;
        double dx = player.getX() - target.getX();
        double dz = player.getZ() - target.getZ();
        target.knockback(knockbackStrength, dx, dz);

        // Spiritus Snare guarantees will drops
        target.addEffect(new MobEffectInstance(NVMobEffects.SPIRITUS_SNARE, 40, 1));
    }

    private static boolean isCatchableProjectileItem(ItemStack stack) {
        return stack.is(Items.ARROW) ||
               stack.is(Items.SPECTRAL_ARROW) ||
               stack.is(Items.TRIDENT) ||
               stack.is(Items.FIREWORK_ROCKET);
    }

    /**
     * Catch projectiles when hit with empty main hand or holding a catchable projectile.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerHurtByProjectile(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        ItemStack mainHandItem = player.getMainHandItem();
        boolean canCatch = mainHandItem.isEmpty() || isCatchableProjectileItem(mainHandItem);
        if (!canCatch) {
            return;
        }

        ItemStack monkSigil = findActiveMonkSigil(player);
        if (monkSigil == null) {
            return;
        }

        if (!(event.getSource().getDirectEntity() instanceof Projectile projectile)) {
            return;
        }

        ItemStack caughtItem = ItemStack.EMPTY;

        if (projectile instanceof Arrow) {
            if (projectile instanceof SpectralArrow) {
                caughtItem = new ItemStack(Items.SPECTRAL_ARROW);
            } else {
                caughtItem = new ItemStack(Items.ARROW);
            }
        } else if (projectile instanceof ThrownTrident) {
            caughtItem = new ItemStack(Items.TRIDENT);
        } else if (projectile instanceof FireworkRocketEntity) {
            caughtItem = new ItemStack(Items.FIREWORK_ROCKET);
        }

        if (!caughtItem.isEmpty()) {
            event.setCanceled(true);

            if (mainHandItem.isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, caughtItem);
            } else if (mainHandItem.is(caughtItem.getItem()) && mainHandItem.getCount() < mainHandItem.getMaxStackSize()) {
                mainHandItem.grow(1);
            } else {
                if (!player.getInventory().add(caughtItem)) {
                    player.drop(caughtItem, false);
                }
            }

            projectile.discard();
            player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                1.0f,
                0.8f + player.getRandom().nextFloat() * 0.4f
            );

            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    player.getX(),
                    player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(),
                    15,
                    0.4, 0.4, 0.4,
                    0.05
                );
                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    player.getX(),
                    player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(),
                    10,
                    0.3, 0.3, 0.3,
                    0.02
                );
            }

            MonkSigilEffect.consumeEV(player, monkSigil, 50);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFallDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!event.getSource().is(DamageTypeTags.IS_FALL)) {
            return;
        }

        ItemStack monkSigil = findActiveMonkSigil(player);
        if (monkSigil == null) {
            return;
        }

        event.setCanceled(true);
        MonkSigilEffect.consumeEV(player, monkSigil, 25);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                player.getX(),
                player.getY(),
                player.getZ(),
                8,
                0.3, 0.1, 0.3,
                0.02
            );
        }
    }

    /**
     * Grant Resistance II and Regeneration I on unarmed kill.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (event.getSource().getDirectEntity() != player) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        if (!hasActiveMonkSigil(player)) {
            return;
        }

        player.addEffect(new MobEffectInstance(
            MobEffects.DAMAGE_RESISTANCE,
            100,
            1
        ));

        player.addEffect(new MobEffectInstance(
            MobEffects.REGENERATION,
            40,
            0
        ));
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        if (!hasActiveMonkSigil(player)) {
            return;
        }

        event.setNewSpeed(MONK_MINING_SPEED);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        if (player == null) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) {
            return;
        }

        ItemStack monkSigil = findActiveMonkSigil(player);
        if (monkSigil == null) {
            return;
        }

        MonkSigilEffect.consumeEV(player, monkSigil, EV_COST_PER_ACTION);
    }

    private static double getTotalSpiritus(Player player) {
        IPlayerSpiritusHandler playerSpiritus = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler();
        double total = 0;
        for (SpiritusType type : SpiritusType.values()) {
            total += playerSpiritus.getTotalSpiritus(type, player);
        }
        return total;
    }

    private static void consumeSpiritus(Player player, double amount) {
        IPlayerSpiritusHandler playerSpiritus = NeoVitaeAPI.getInstance().getPlayerSpiritusHandler();
        double remaining = amount;

        // Prefer Raw Spiritus first, then other types
        SpiritusType[] preferredOrder = {
            SpiritusType.RAW,
            SpiritusType.VINDICTA,
            SpiritusType.INVICTUS,
            SpiritusType.RUINA,
            SpiritusType.NIHILUM
        };

        for (SpiritusType type : preferredOrder) {
            if (remaining <= 0) break;

            double available = playerSpiritus.getTotalSpiritus(type, player);
            if (available > 0) {
                double toConsume = Math.min(remaining, available);
                playerSpiritus.consumeSpiritus(type, player, toConsume);
                remaining -= toConsume;
            }
        }
    }
}
