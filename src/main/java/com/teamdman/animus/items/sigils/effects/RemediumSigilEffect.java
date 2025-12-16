package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.util.SigilStateTracker;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.api.sigil.ISigilEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * Sigil of Remedium - continuously cleanses negative status effects.
 * While active, removes all negative effects once per second (20 ticks).
 * LP cost is determined by the sigil_type JSON (cost per tick when active).
 */
public record RemediumSigilEffect() implements ISigilEffect {
    public static final MapCodec<RemediumSigilEffect> CODEC = MapCodec.unit(RemediumSigilEffect::new);

    // Track last cleanse tick per player (auto-registered for cleanup)
    private static final SigilStateTracker TRACKER = new SigilStateTracker("remedium");
    private static final int CLEANSE_INTERVAL = 20; // 1 second

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean isToggleable() {
        return true;
    }

    @Override
    public void activeTick(Level level, Player player, ItemStack stack, int itemSlot, boolean isSelected) {
        if (level.isClientSide) {
            return;
        }

        // Check if enough time has passed using the tracker
        long currentTime = level.getGameTime();
        if (!TRACKER.isReady(player.getUUID(), currentTime, CLEANSE_INTERVAL)) {
            return;
        }

        // Collect all negative effects
        List<Holder<MobEffect>> negativeEffects = new ArrayList<>();
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            Holder<MobEffect> effect = effectInstance.getEffect();
            if (!effect.value().isBeneficial()) {
                negativeEffects.add(effect);
            }
        }

        // If no negative effects, nothing to do
        if (negativeEffects.isEmpty()) {
            return;
        }

        // Remove all negative effects
        // Note: LP cost is handled by the sigil system based on sigil_type JSON
        for (Holder<MobEffect> effect : negativeEffects) {
            player.removeEffect(effect);
        }

        TRACKER.updateTime(player.getUUID(), currentTime);
    }

    // Cleanup is handled automatically by SigilStateCleanupManager via TRACKER registration
}
