package com.teamdman.animus.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.will.WorldDemonWillHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Hand of Death - The ultimate sentient scythe forged from demon steel
 *
 * Features:
 * - Extends Runic Sentient Scythe with +5 base damage
 * - Lifesteal: Heals 20% of damage dealt (minimum 1 health)
 * - Execute: Instantly kills targets below 15% health with a second strike
 * - Requires Demon Forged Steel to craft
 * - All features from Runic Sentient Scythe (30% faster attack speed, Malum integration)
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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create(super.getAttributeModifiers(slot, stack));

        if (slot == EquipmentSlot.MAINHAND) {
            // Remove existing attack damage modifier from parent
            multimap.removeAll(Attributes.ATTACK_DAMAGE);

            // Get current demon will type and cached soul count from stack NBT
            EnumWillType type = getCurrentType(stack);
            double soulsRemaining = 0;

            // Get cached soul count from NBT if available
            if (stack.getTag() != null && stack.getTag().contains("cachedSouls")) {
                soulsRemaining = stack.getTag().getDouble("cachedSouls");
            }

            int level = getLevel(stack, soulsRemaining);

            // Get base damage from demon will type and add our bonus
            double baseDamage = getDamageAdded(type, level) + BONUS_DAMAGE;

            multimap.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                BASE_ATTACK_DAMAGE_UUID,
                "Weapon modifier",
                baseDamage,
                AttributeModifier.Operation.ADDITION
            ));
        }

        return multimap;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Cache soul count for damage calculation (must be done before parent call)
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            // Get total demon will from all types
            double totalWill = 0;
            for (EnumWillType type : EnumWillType.values()) {
                totalWill += WorldDemonWillHandler.getCurrentWill(attacker.level(), player.blockPosition(), type);
            }
            if (stack.getTag() == null) {
                stack.setTag(new net.minecraft.nbt.CompoundTag());
            }
            stack.getTag().putDouble("cachedSouls", totalWill);
        }

        // Call parent to apply normal sentient scythe damage and effects
        // (parent applies Soul Snare effect - 5 seconds, amplifier 1)
        boolean result = super.hurtEnemy(stack, target, attacker);

        if (attacker.level().isClientSide || !(attacker instanceof Player player)) {
            return result;
        }

        Level level = attacker.level();

        // Calculate damage dealt for lifesteal
        // We need to track the damage, but since we can't directly get it,
        // we'll estimate it based on the weapon's attack damage
        float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);

        // Apply lifesteal - heal player for 20% of damage dealt (minimum 1)
        float healAmount = Math.max(1.0f, attackDamage * LIFESTEAL_PERCENT);
        player.heal(healAmount);

        // Spawn healing particles around player
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

        // Check if target is still alive and below execute threshold
        if (target.isAlive()) {
            float currentHealth = target.getHealth();
            float maxHealth = target.getMaxHealth();
            float healthPercent = currentHealth / maxHealth;

            if (healthPercent <= EXECUTE_THRESHOLD) {
                // EXECUTE! Deal damage equal to max health (instant kill)
                executeTarget(target, maxHealth, level, player);
            }
        }

        return result;
    }

    /**
     * Execute a low-health target with dramatic effects
     */
    private void executeTarget(LivingEntity target, float maxHealth, Level level, Player executioner) {
        if (level.isClientSide) {
            return;
        }

        // Deal damage equal to max health (guaranteed kill)
        target.hurt(level.damageSources().playerAttack(executioner), maxHealth);

        ServerLevel serverLevel = (ServerLevel) level;

        // Spawn soul speed particles (rising spiral effect)
        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();

        // Create a spiral of soul speed particles
        for (int i = 0; i < 30; i++) {
            double angle = (i / 30.0) * Math.PI * 4; // 2 full rotations
            double radius = 0.5;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = (i / 30.0) * 2.0; // Rise 2 blocks

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

        // Additional burst of soul particles
        serverLevel.sendParticles(
            ParticleTypes.SOUL,
            targetX,
            targetY + 1.0,
            targetZ,
            20,
            0.5, 0.5, 0.5,
            0.1
        );

        // Play execute sound (allay death at very low pitch and quiet volume)
        level.playSound(
            null,
            target.getX(),
            target.getY(),
            target.getZ(),
            SoundEvents.ALLAY_DEATH,
            SoundSource.HOSTILE,
            0.05f, // 5% volume
            0.01f  // 1% pitch (very deep and ominous)
        );

        // Optional: Display execute message to player
        if (executioner != null) {
            executioner.displayClientMessage(
                Component.translatable("text.component.animus.hand_of_death.execute", target.getName())
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                true
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        // Add title tooltip first
        tooltip.add(Component.translatable("tooltip.animus.hand_of_death.ultimate")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        // Call parent for basic sentient scythe tooltips (includes demon will damage)
        super.appendHoverText(stack, level, tooltip, flag);

        // Show Hand of Death bonus damage (not total, parent already shows will damage)
        tooltip.add(Component.literal(String.format("Hand of Death Bonus: +%.1f", BONUS_DAMAGE))
            .withStyle(ChatFormatting.RED));

        // Add Hand of Death specific tooltips
        tooltip.add(Component.translatable("tooltip.animus.hand_of_death.lifesteal")
            .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.animus.hand_of_death.execute")
            .withStyle(ChatFormatting.DARK_PURPLE));
    }

    // Helper method to get damage added by demon will
    // Uses destructive will values regardless of type (highest damage)
    private static double getDamageAdded(EnumWillType type, int level) {
        level = Math.min(level, 6);

        // Use destructive will damage scaling for all types
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
