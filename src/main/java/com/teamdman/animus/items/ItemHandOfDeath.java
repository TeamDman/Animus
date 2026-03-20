package com.teamdman.animus.items;

import com.teamdman.animus.compat.CompatHandler;
import com.teamdman.animus.compat.malum.SpiritHarvestHelper;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.common.datacomponent.EnumWillType;
import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import com.breakinblocks.neovitae.will.PlayerDemonWillHandler;

import java.util.List;

/**
 * Hand of Death - The ultimate sentient scythe forged from demon steel
 *
 * Features:
 * - Extends Runic Sentient Scythe with +5 base damage
 * - Lifesteal: Heals 20% of damage dealt (minimum 1 health)
 * - Execute: Instantly kills targets below 15% health with a second strike
 * - Requires Demon Forged Steel to craft
 * - All features from Runic Sentient Scythe (Malum integration)
 */
public class ItemHandOfDeath extends ItemRunicSentientScythe {
    // Additional damage bonus over Runic Sentient Scythe
    private static final double BONUS_DAMAGE = 14.0;

    // Lifesteal percentage (20% of damage dealt)
    private static final float LIFESTEAL_PERCENT = 0.20f;

    // Execute threshold (15% of max health)
    private static final float EXECUTE_THRESHOLD = 0.15f;

    public ItemHandOfDeath() {
        super();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Must cache soul count before parent call modifies will
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            double totalWill = 0;
            for (EnumWillType type : EnumWillType.values()) {
                totalWill += PlayerDemonWillHandler.getTotalDemonWill(type, player);
            }
            setCachedSouls(stack, totalWill);

            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                Holder.direct(NVMobEffects.SOUL_SNARE.get()), 100, 1));
        }

        boolean result = super.hurtEnemy(stack, target, attacker);

        if (attacker.level().isClientSide || !(attacker instanceof Player player)) {
            return result;
        }

        Level level = attacker.level();

        float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float healAmount = Math.max(1.0f, attackDamage * LIFESTEAL_PERCENT);
        player.heal(healAmount);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.HEART,
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                3,
                0.5, 0.5, 0.5,
                0.1
            );
        }

        if (target.isAlive()) {
            float currentHealth = target.getHealth();
            float maxHealth = target.getMaxHealth();
            float healthPercent = currentHealth / maxHealth;

            if (healthPercent <= EXECUTE_THRESHOLD) {
                executeTarget(target, maxHealth, level, player, stack);
            }
        }

        return result;
    }

    private void executeTarget(LivingEntity target, float maxHealth, Level level, Player executioner, ItemStack weapon) {
        if (level.isClientSide) {
            return;
        }

        target.hurt(level.damageSources().playerAttack(executioner), maxHealth);

        // Parent's spirit harvest check ran before execute, so handle it here
        if (CompatHandler.isMalumLoaded() && target.isDeadOrDying()) {
            SpiritHarvestHelper.harvestSpirits(target, executioner, weapon);
        }

        ServerLevel serverLevel = (ServerLevel) level;

        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();

        for (int i = 0; i < 30; i++) {
            double angle = (i / 30.0) * Math.PI * 4;
            double radius = 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = (i / 30.0) * 2.0;

            serverLevel.sendParticles(
                ParticleTypes.SOUL,
                targetX + offsetX,
                targetY + offsetY,
                targetZ + offsetZ,
                1,
                0.0, 0.0, 0.0,
                0.02
            );
        }

        serverLevel.sendParticles(
            ParticleTypes.SOUL,
            targetX,
            targetY + 1.0,
            targetZ,
            20,
            0.5, 0.5, 0.5,
            0.1
        );

        level.playSound(
            null,
            target.getX(),
            target.getY(),
            target.getZ(),
            SoundEvents.ALLAY_DEATH,
            SoundSource.HOSTILE,
            0.05f,
            0.01f
        );

        if (executioner != null) {
            executioner.displayClientMessage(
                Component.translatable("text.component.animus.hand_of_death.execute", target.getName())
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                true
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.animus.hand_of_death.ultimate")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.literal(String.format("Hand of Death Bonus: +%.1f", BONUS_DAMAGE))
            .withStyle(ChatFormatting.RED));

        tooltip.add(Component.translatable("tooltip.animus.hand_of_death.lifesteal")
            .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.animus.hand_of_death.execute")
            .withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static double getDamageAdded(EnumWillType type, int level) {
        level = Math.min(level, 6);

        double[] damageAdded = new double[]{5.0, 6.5, 8.0, 9.5, 11.0, 12.5, 14.0};
        return damageAdded[level];
    }

    private static int getLevel(ItemStack stack, double soulsRemaining) {
        double[] soulBracket = new double[]{16, 60, 200, 400, 1000, 2000, 4000};

        for (int i = 0; i < soulBracket.length; i++) {
            if (soulsRemaining < soulBracket[i]) {
                return i;
            }
        }
        return soulBracket.length;
    }
}
