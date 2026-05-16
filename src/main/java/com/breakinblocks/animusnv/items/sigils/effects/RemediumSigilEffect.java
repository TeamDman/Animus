package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.util.SigilStateTracker;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record RemediumSigilEffect() implements ISigilEffect {
    public static final MapCodec<RemediumSigilEffect> CODEC = MapCodec.unit(RemediumSigilEffect::new);

    private static final SigilStateTracker TRACKER = new SigilStateTracker("remedium");
    private static final int CLEANSE_INTERVAL = 20;

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

        List<Holder<MobEffect>> negativeEffects = new ArrayList<>();
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            Holder<MobEffect> effect = effectInstance.getEffect();
            if (!effect.value().isBeneficial()) {
                negativeEffects.add(effect);
            }
        }

        if (negativeEffects.isEmpty()) {
            return;
        }

        for (Holder<MobEffect> effect : negativeEffects) {
            player.removeEffect(effect);
        }

        TRACKER.updateTime(player.getUUID(), currentTime);
    }

    @Override
    public void onPlayerLogout(UUID playerId, MinecraftServer server) {
        TRACKER.cleanup(playerId);
    }

}
