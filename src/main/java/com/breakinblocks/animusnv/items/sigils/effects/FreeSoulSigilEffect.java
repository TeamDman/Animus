package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.neovitae.common.datacomponent.Binding;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record FreeSoulSigilEffect() implements ISigilEffect {
    public static final MapCodec<FreeSoulSigilEffect> CODEC = MapCodec.unit(FreeSoulSigilEffect::new);

    private static final Map<UUID, SpectatorState> activeSpectators = new HashMap<>();

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return false;
        }

        // Check if player is already in spectator mode
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                player.sendOverlayMessage(
                        Component.translatable(Constants.Localizations.Text.FREE_SOUL_ALREADY_SPECTATOR)
                                .withStyle(ChatFormatting.RED));
                return false;
            }

            int evCost = AnimusConfig.sigils.freeSoulEVCost.get();

            Binding binding = stack.get(NVDataComponents.BINDING.get());
            IAnima network = binding != null
                ? NeoVitaeAPI.getInstance().getAnima(binding.uuid())
                : NeoVitaeAPI.getInstance().getAnima(player.getUUID());
            AnimaTicket ticket = AnimaTicket.create(evCost);

            var syphonResult = network.syphonAndDamage(player, ticket);
            if (!syphonResult.success()) {
                player.sendOverlayMessage(
                        Component.translatable(Constants.Localizations.Text.FREE_SOUL_NO_EV)
                                .withStyle(ChatFormatting.RED));
                return false;
            }

            activateSpectatorMode(serverPlayer, (ServerLevel) level, false);

            // Return false - EV cost handled above, not via sigil_type
            return false;
        }

        return false;
    }

    public static boolean tryPreventDeath(Player player, DamageSource source, ItemStack freeSoulStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        Long lastTriggerObj = freeSoulStack.get(AnimusDataComponents.LAST_DEATH_PREVENT.get());
        long lastTrigger = lastTriggerObj != null ? lastTriggerObj : 0L;
        long currentTime = System.currentTimeMillis();
        int cooldownSeconds = AnimusConfig.sigils.freeSoulCooldown.get();
        long cooldownMillis = cooldownSeconds * 1000L;

        if (lastTrigger > 0 && (currentTime - lastTrigger) < cooldownMillis) {
            long remainingSeconds = (cooldownMillis - (currentTime - lastTrigger)) / 1000;
            player.sendOverlayMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_ON_COOLDOWN, remainingSeconds)
                            .withStyle(ChatFormatting.RED));
            return false;
        }

        int evCost = AnimusConfig.sigils.freeSoulEVCost.get();
        Binding binding = freeSoulStack.get(NVDataComponents.BINDING.get());
        IAnima network = binding != null
            ? NeoVitaeAPI.getInstance().getAnima(binding.uuid())
            : NeoVitaeAPI.getInstance().getAnima(player.getUUID());

        if (network.getCurrentEV() < evCost) {
            return false;
        }

        var syphonResult = network.syphonAndDamage(player, AnimaTicket.create(evCost));
        if (!syphonResult.success()) {
            return false;
        }

        freeSoulStack.set(AnimusDataComponents.LAST_DEATH_PREVENT.get(), currentTime);
        player.setHealth(1.0F);

        ServerLevel level = serverPlayer.level();
        activateSpectatorMode(serverPlayer, level, true);

        player.sendOverlayMessage(
                Component.translatable(Constants.Localizations.Text.FREE_SOUL_SAVED)
                        .withStyle(ChatFormatting.GOLD));

        return true;
    }

    private static void activateSpectatorMode(ServerPlayer player, ServerLevel level, boolean fromDeath) {
        GameType previousGameMode = player.gameMode.getGameModeForPlayer();
        Vec3 originalPosition = player.position();

        int durationSeconds = AnimusConfig.sigils.freeSoulDuration.get();
        long exitTick = level.getServer().getTickCount() + (durationSeconds * 20L);

        activeSpectators.put(player.getUUID(), new SpectatorState(exitTick, previousGameMode, originalPosition, fromDeath));
        player.setGameMode(GameType.SPECTATOR);

        if (!fromDeath) {
            player.sendOverlayMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_ACTIVATED, durationSeconds)
                            .withStyle(ChatFormatting.AQUA));
        }
    }

    public static void tickActiveSpectators(ServerPlayer player, ServerLevel level) {
        UUID playerId = player.getUUID();
        SpectatorState state = activeSpectators.get(playerId);

        if (state == null) {
            return;
        }

        long currentTick = level.getServer().getTickCount();

        // Teleport back 10 ticks early to prevent wall-clipping exploits
        if (!state.hasTeleportedBack && currentTick >= state.exitTick - 10) {
            player.teleportTo(
                    state.originalPosition.x,
                    state.originalPosition.y,
                    state.originalPosition.z
            );
            state.hasTeleportedBack = true;

            player.sendOverlayMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_RETURNING)
                            .withStyle(ChatFormatting.GOLD));
        }

        if (currentTick >= state.exitTick) {
            player.setGameMode(state.previousGameMode);

            if (state.fromDeath) {
                float newHealth = Math.min(player.getHealth() + 10.0F, player.getMaxHealth());
                player.setHealth(newHealth);
            }

            activeSpectators.remove(playerId);

            player.sendOverlayMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_EXPIRED)
                            .withStyle(ChatFormatting.YELLOW));
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        UUID playerId = player.getUUID();
        SpectatorState state = activeSpectators.remove(playerId);

        if (state != null) {
            player.setGameMode(state.previousGameMode);
        }
    }

    public static boolean isInSpectatorMode(UUID playerId) {
        return activeSpectators.containsKey(playerId);
    }

    /**
     * Tracks spectator state for a player.
     */
    private static class SpectatorState {
        final long exitTick;
        final GameType previousGameMode;
        final Vec3 originalPosition;
        final boolean fromDeath;
        boolean hasTeleportedBack = false;

        SpectatorState(long exitTick, GameType previousGameMode, Vec3 originalPosition, boolean fromDeath) {
            this.exitTick = exitTick;
            this.previousGameMode = previousGameMode;
            this.originalPosition = originalPosition;
            this.fromDeath = fromDeath;
        }
    }
}
