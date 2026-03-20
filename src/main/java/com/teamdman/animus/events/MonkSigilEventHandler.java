package com.teamdman.animus.events;

import com.teamdman.animus.Constants;
import com.teamdman.animus.items.sigils.effects.MonkSigilEffect;
import com.teamdman.animus.registry.AnimusAttributes;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import com.breakinblocks.neovitae.api.soul.SoulTicket;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.ISoulNetwork;
import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import com.breakinblocks.neovitae.api.will.IPlayerDemonWillHandler;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class MonkSigilEventHandler {

    private static final double MIN_WILL_FOR_EXECUTE = 1.0;
    private static final double MAX_WILL_FOR_EXECUTE = 4096.0;
    private static final double MIN_EXECUTE_PERCENT = 0.01; // 1%
    private static final double MAX_EXECUTE_PERCENT = 0.15; // 15%
    private static final double WILL_COST_PER_EXECUTE = 5.0;
    private static final int LP_REWARD_PER_EXECUTE = 200;
    private static final int LP_COST_PER_ACTION = 5;

    // Netherite pickaxe base (9.0) + Efficiency V (level^2 + 1 = 26) = 35.0
    private static final float MONK_MINING_SPEED = 35.0f;

    public static boolean hasActiveMonkSigil(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_MONK.get())) {
                if (stack.getItem() instanceof com.breakinblocks.neovitae.common.item.IActivatable activatable) {
                    if (activatable.getActivated(stack)) {
                        return true;
                    }
                }
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty() && stack.is(AnimusItems.SIGIL_MONK.get())) {
                if (stack.getItem() instanceof com.breakinblocks.neovitae.common.item.IActivatable activatable) {
                    if (activatable.getActivated(stack)) {
                        return true;
                    }
                }
            }
        }
        return false;
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

        if (!hasActiveMonkSigil(player)) {
            return;
        }

        double unarmedDamage = player.getAttributeValue(AnimusAttributes.UNARMED_DAMAGE);
        if (unarmedDamage <= 0) {
            return;
        }

        net.minecraft.world.entity.LivingEntity target = event.getEntity();
        float targetMaxHealth = target.getMaxHealth();
        float baseDamage = event.getAmount() + (float) unarmedDamage;

        double totalWill = getTotalDemonWill(player);

        // Execute scales linearly: 1% at 1 will to 15% at 4096 will
        double executePercent = 0;
        float willBonusDamage = 0;

        if (totalWill >= MIN_WILL_FOR_EXECUTE) {
            double clampedWill = Math.min(totalWill, MAX_WILL_FOR_EXECUTE);
            double willRatio = (clampedWill - MIN_WILL_FOR_EXECUTE) / (MAX_WILL_FOR_EXECUTE - MIN_WILL_FOR_EXECUTE);
            executePercent = MIN_EXECUTE_PERCENT + willRatio * (MAX_EXECUTE_PERCENT - MIN_EXECUTE_PERCENT);
            willBonusDamage = (float) (targetMaxHealth * executePercent);
        }

        float totalDamage = baseDamage + willBonusDamage;
        event.setAmount(totalDamage);

        float healthAfterAttack = target.getHealth() - totalDamage;
        float executeThreshold = (float) (targetMaxHealth * executePercent);

        MonkSigilEffect.consumeLP(player, LP_COST_PER_ACTION);

        // Execute: if target would survive but be below threshold, finish them off
        if (executePercent > 0 && healthAfterAttack > 0 && healthAfterAttack < executeThreshold) {
            if (totalWill >= WILL_COST_PER_EXECUTE) {
                consumeDemonWill(player, WILL_COST_PER_EXECUTE);

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
                        20, // count
                        0.5, 0.5, 0.5, // spread
                        0.1 // speed
                    );
                }

                ISoulNetwork network = NeoVitaeAPI.getInstance().getSoulNetwork(player.getUUID());
                if (network != null) {
                    network.add(SoulTicket.create(LP_REWARD_PER_EXECUTE), LP_REWARD_PER_EXECUTE);
                }
            }
        }

        double knockbackStrength = 0.5D;
        double dx = player.getX() - target.getX();
        double dz = player.getZ() - target.getZ();
        target.knockback(knockbackStrength, dx, dz);

        // Soul Snare guarantees will drops
        target.addEffect(new MobEffectInstance(NVMobEffects.SOUL_SNARE, 40, 1));
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

        if (!hasActiveMonkSigil(player)) {
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
                    15, // count
                    0.4, 0.4, 0.4, // spread
                    0.05 // speed
                );
                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    player.getX(),
                    player.getY() + player.getBbHeight() / 2.0,
                    player.getZ(),
                    10, // count
                    0.3, 0.3, 0.3, // spread
                    0.02 // speed
                );
            }

            MonkSigilEffect.consumeLP(player, 50);
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

        if (!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
            return;
        }

        if (!hasActiveMonkSigil(player)) {
            return;
        }

        event.setCanceled(true);
        MonkSigilEffect.consumeLP(player, 25);
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

        if (!hasActiveMonkSigil(player)) {
            return;
        }

        MonkSigilEffect.consumeLP(player, LP_COST_PER_ACTION);
    }

    private static double getTotalDemonWill(Player player) {
        IPlayerDemonWillHandler playerWill = NeoVitaeAPI.getInstance().getPlayerWillHandler();
        double total = 0;
        for (EnumWillType type : EnumWillType.values()) {
            total += playerWill.getTotalDemonWill(type, player);
        }
        return total;
    }

    private static void consumeDemonWill(Player player, double amount) {
        IPlayerDemonWillHandler playerWill = NeoVitaeAPI.getInstance().getPlayerWillHandler();
        double remaining = amount;

        // Prefer raw will first, then other types
        EnumWillType[] preferredOrder = {
            EnumWillType.DEFAULT,
            EnumWillType.VENGEFUL,
            EnumWillType.STEADFAST,
            EnumWillType.CORROSIVE,
            EnumWillType.DESTRUCTIVE
        };

        for (EnumWillType type : preferredOrder) {
            if (remaining <= 0) break;

            double available = playerWill.getTotalDemonWill(type, player);
            if (available > 0) {
                double toConsume = Math.min(remaining, available);
                playerWill.consumeDemonWill(type, player, toConsume);
                remaining -= toConsume;
            }
        }
    }
}
