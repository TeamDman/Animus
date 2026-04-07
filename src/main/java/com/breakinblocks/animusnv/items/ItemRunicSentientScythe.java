package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.compat.CompatHandler;
import com.breakinblocks.animusnv.compat.malum.SpiritHarvestHelper;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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

            // Spawn swing particle and sound effect
            if (attacker.level() instanceof ServerLevel serverLevel) {
                spawnSwingEffect(serverLevel, attacker, target);
            }
        }

        boolean result = super.hurtEnemy(stack, target, attacker);

        if (CompatHandler.isMalumLoaded() && attacker instanceof Player player && target.isDeadOrDying()) {
            SpiritHarvestHelper.harvestSpirits(target, player, stack);
        }

        return result;
    }

    /**
     * Spawn scythe swing particle and sound effects on hit.
     * Produces an arc of soul fire particles in front of the attacker.
     * Override in subclasses for different visual themes.
     */
    protected void spawnSwingEffect(ServerLevel serverLevel, LivingEntity attacker, LivingEntity target) {
        // Play sweep sound at lower pitch for heavy scythe feel
        serverLevel.playSound(
            null,
            attacker.getX(), attacker.getY(), attacker.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP,
            SoundSource.PLAYERS,
            1.0f, 0.6f
        );

        // Compute player-relative directions
        float yawRad = attacker.getYRot() * Mth.DEG_TO_RAD;
        double forwardX = -Mth.sin(yawRad);
        double forwardZ = Mth.cos(yawRad);
        double rightX = -Mth.cos(yawRad);
        double rightZ = -Mth.sin(yawRad);

        double centerX = attacker.getX();
        double centerY = attacker.getY() + attacker.getBbHeight() * 0.5;
        double centerZ = attacker.getZ();
        double radius = 1.8;

        int particleCount = 16;
        float arcSpanRad = 150f * Mth.DEG_TO_RAD; // 150 degree arc

        for (int i = 0; i < particleCount; i++) {
            float progress = (float) i / (particleCount - 1);
            float angle = -arcSpanRad / 2 + arcSpanRad * progress;

            // Direction combining forward and sideways components
            double dirX = forwardX * Mth.cos(angle) + rightX * Mth.sin(angle);
            double dirZ = forwardZ * Mth.cos(angle) + rightZ * Mth.sin(angle);

            double px = centerX + dirX * radius;
            double pz = centerZ + dirZ * radius;

            // Arc upward in the middle for a curved slash shape
            double heightOffset = Mth.sin(progress * (float) Math.PI) * 0.4;

            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                px, centerY + heightOffset, pz,
                1,
                0.0, 0.0, 0.0,
                0.01
            );
        }
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
