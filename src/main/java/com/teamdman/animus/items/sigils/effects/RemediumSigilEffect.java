package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
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

    // Track last cleanse tick per player (using game time)
    private static final java.util.Map<java.util.UUID, Long> lastCleanseTime = new java.util.concurrent.ConcurrentHashMap<>();

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

        // Check if enough time has passed (1 second = 20 ticks)
        long currentTime = level.getGameTime();
        Long lastCleanse = lastCleanseTime.get(player.getUUID());
        if (lastCleanse != null && currentTime - lastCleanse < 20) {
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

        lastCleanseTime.put(player.getUUID(), currentTime);
    }

    /**
     * Clean up tracking data when player logs out.
     */
    public static void onPlayerLogout(java.util.UUID playerId) {
        lastCleanseTime.remove(playerId);
    }
}
