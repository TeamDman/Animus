package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.malum.SpiritHarvestHelper;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.effect.NVMobEffects;
import com.breakinblocks.neovitae.common.item.soul.SentientScytheItem;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.will.IPlayerSpiritusHandler;

import java.util.List;

/**
 * Runic Sentient Scythe - A cross-mod compatibility weapon combining NeoVitae and Malum
 *
 * Features:
 * - Extends NeoVitae's Sentient Scythe with enhanced attack speed
 * - Integrates Malum's soul harvesting when Malum is loaded
 * - Full Spiritus integration from NeoVitae
 */
public class ItemRunicSentientScythe extends SentientScytheItem {

    public ItemRunicSentientScythe() {
        super();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Must cache soul count before parent call modifies will
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            IPlayerSpiritusHandler playerWill = NeoVitaeAPI.getInstance().getPlayerWillHandler();
            double totalWill = 0;
            for (SpiritusType type : SpiritusType.values()) {
                totalWill += playerWill.getTotalSpiritus(type, player);
            }
            stack.set(AnimusDataComponents.CACHED_SOULS.get(), totalWill);

            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                Holder.direct(NVMobEffects.SPIRITUS_SNARE.get()), 100, 1));
        }

        boolean result = super.hurtEnemy(stack, target, attacker);

        if (CompatHandler.isMalumLoaded() && attacker instanceof Player player && target.isDeadOrDying()) {
            SpiritHarvestHelper.harvestSpirits(target, player, stack);
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        double soulsRemaining = getCachedSouls(stack);
        int willLevel = getLevel(soulsRemaining);
        double willDamage = getDamageAdded(willLevel);

        tooltip.add(Component.literal(String.format("Spiritus Damage: +%.1f", willDamage))
            .withStyle(ChatFormatting.LIGHT_PURPLE));

        tooltip.add(Component.translatable("tooltip.animusnv.runic_sentient_scythe.enhanced")
            .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.animusnv.runic_sentient_scythe.attack_speed")
            .withStyle(ChatFormatting.GREEN));

        if (CompatHandler.isMalumLoaded()) {
            tooltip.add(Component.translatable("tooltip.animusnv.runic_sentient_scythe.malum")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    public double getCachedSouls(ItemStack stack) {
        Double souls = stack.get(AnimusDataComponents.CACHED_SOULS.get());
        return souls != null ? souls : 0.0;
    }

    public void setCachedSouls(ItemStack stack, double souls) {
        stack.set(AnimusDataComponents.CACHED_SOULS.get(), souls);
    }

    protected static double getDamageAdded(int level) {
        level = Math.min(level, 6);
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
