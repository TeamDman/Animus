package com.teamdman.animus.items;

import com.teamdman.animus.compat.CompatHandler;
import com.teamdman.animus.compat.malum.SpiritHarvestHelper;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.effect.BMMobEffects;
import wayoftime.bloodmagic.common.item.soul.SentientScytheItem;
import wayoftime.bloodmagic.will.PlayerDemonWillHandler;

import java.util.List;

/**
 * Runic Sentient Scythe - A cross-mod compatibility weapon combining Blood Magic and Malum
 *
 * Features:
 * - Extends Blood Magic's Sentient Scythe with enhanced attack speed
 * - Integrates Malum's soul harvesting when Malum is loaded
 * - Full demon will integration from Blood Magic
 */
public class ItemRunicSentientScythe extends SentientScytheItem {

    public ItemRunicSentientScythe() {
        super();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Cache total soul count for damage calculation (must be done before parent call)
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            // Get total demon will from all types
            double totalWill = 0;
            for (EnumWillType type : EnumWillType.values()) {
                totalWill += PlayerDemonWillHandler.getTotalDemonWill(type, player);
            }
            stack.set(AnimusDataComponents.CACHED_SOULS.get(), totalWill);

            // Apply Soul Snare effect (5 seconds, amplifier 1) for guaranteed will drops
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                Holder.direct(BMMobEffects.SOUL_SNARE.get()), 100, 1));
        }

        boolean result = super.hurtEnemy(stack, target, attacker);

        // Trigger Malum soul harvesting if available (only when target is killed)
        if (CompatHandler.isMalumLoaded() && attacker instanceof Player player && target.isDeadOrDying()) {
            SpiritHarvestHelper.harvestSpirits(target, player, stack);
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        // Show demon will damage bonus
        double soulsRemaining = getCachedSouls(stack);
        int willLevel = getLevel(soulsRemaining);
        double willDamage = getDamageAdded(willLevel);

        tooltip.add(Component.literal(String.format("Demon Will Damage: +%.1f", willDamage))
            .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltip.add(Component.translatable("tooltip.animus.runic_sentient_scythe.enhanced")
            .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.animus.runic_sentient_scythe.attack_speed")
            .withStyle(ChatFormatting.GREEN));

        if (CompatHandler.isMalumLoaded()) {
            tooltip.add(Component.translatable("tooltip.animus.runic_sentient_scythe.malum")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    /**
     * Get cached soul count from data component
     */
    public double getCachedSouls(ItemStack stack) {
        Double souls = stack.get(AnimusDataComponents.CACHED_SOULS.get());
        return souls != null ? souls : 0.0;
    }

    /**
     * Set cached soul count in data component
     */
    public void setCachedSouls(ItemStack stack, double souls) {
        stack.set(AnimusDataComponents.CACHED_SOULS.get(), souls);
    }

    // Helper method to get damage added by demon will
    // Uses destructive will values regardless of type (highest damage)
    protected static double getDamageAdded(int level) {
        level = Math.min(level, 6);
        // Use destructive will damage scaling
        double[] damageAdded = new double[]{5.0, 6.5, 8.0, 9.5, 11.0, 12.5, 14.0};
        return damageAdded[level];
    }

    protected static int getLevel(double soulsRemaining) {
        double[] soulBracket = new double[]{16, 60, 200, 400, 1000, 2000, 4000};

        for (int i = 0; i < soulBracket.length; i++) {
            if (soulsRemaining < soulBracket[i]) {
                return i;
            }
        }
        return soulBracket.length;
    }
}
