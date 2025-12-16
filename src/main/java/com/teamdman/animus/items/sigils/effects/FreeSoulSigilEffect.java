package com.teamdman.animus.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusDataComponents;
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
import wayoftime.bloodmagic.api.sigil.ISigilEffect;
import wayoftime.bloodmagic.common.datacomponent.Binding;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.api.soul.SoulTicket;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sigil of the Free Soul - Grants temporary spectator mode and prevents death.
 * <p>
 * Features:
 * - Right-click to manually enter spectator mode for configurable duration (default: 10 seconds)
 * - LP cost is handled in this effect (not delegated to sigil_type JSON due to special behavior)
 * - Automatically activates on death (like totem of undying) if player has enough LP
 * - Cannot trigger death prevention again for configurable cooldown (default: 60 seconds)
 * - Teleports player back to original position before spectator mode ends
 * - Heals player 10 health ONLY when death prevention is triggered
 * <p>
 * Death prevention hooked up in AnimusEventHandler.onLivingDeath()
 * Spectator mode timer hooked up in AnimusEventHandler.onPlayerTick()
 */
public record FreeSoulSigilEffect() implements ISigilEffect {
    public static final MapCodec<FreeSoulSigilEffect> CODEC = MapCodec.unit(FreeSoulSigilEffect::new);

    // Track players currently in spectator mode - map of player UUID to exit time
    private static final Map<UUID, SpectatorState> activeSpectators = new HashMap<>();

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        if (level.isClientSide) {
            return false;
        }

        // Check if player is already in spectator mode
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.FREE_SOUL_ALREADY_SPECTATOR)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return false;
            }

            // Get LP cost from config
            int lpCost = AnimusConfig.sigils.freeSoulLPCost.get();

            // Try to consume LP
            SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
            SoulTicket ticket = SoulTicket.create(lpCost);

            var syphonResult = network.syphonAndDamage(player, ticket);
            if (!syphonResult.success()) {
                // Not enough LP
                player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.FREE_SOUL_NO_LP)
                                .withStyle(ChatFormatting.RED),
                        true
                );
                return false;
            }

            // Activate spectator mode
            activateSpectatorMode(serverPlayer, (ServerLevel) level, false);

            // Return false to skip LP cost from sigil_type (we handle it ourselves)
            return false;
        }

        return false;
    }

    /**
     * Try to prevent death by activating spectator mode.
     * Returns true if death was prevented, false otherwise.
     * Called from AnimusEventHandler.onLivingDeath()
     */
    public static boolean tryPreventDeath(Player player, DamageSource source, ItemStack freeSoulStack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        // Check cooldown
        Long lastTriggerObj = freeSoulStack.get(AnimusDataComponents.LAST_DEATH_PREVENT.get());
        long lastTrigger = lastTriggerObj != null ? lastTriggerObj : 0L;
        long currentTime = System.currentTimeMillis();
        int cooldownSeconds = AnimusConfig.sigils.freeSoulCooldown.get();
        long cooldownMillis = cooldownSeconds * 1000L;

        if (lastTrigger > 0 && (currentTime - lastTrigger) < cooldownMillis) {
            // Still on cooldown
            long remainingSeconds = (cooldownMillis - (currentTime - lastTrigger)) / 1000;
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_ON_COOLDOWN, remainingSeconds)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Check if player has enough LP
        int lpCost = AnimusConfig.sigils.freeSoulLPCost.get();
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(player);
        int currentEssence = network.getCurrentEssence();

        if (currentEssence < lpCost) {
            // Not enough LP
            return false;
        }

        // Consume LP
        SoulTicket ticket = SoulTicket.create(lpCost);
        var syphonResult = network.syphonAndDamage(player, ticket);
        if (!syphonResult.success()) {
            return false;
        }

        // Set cooldown
        freeSoulStack.set(AnimusDataComponents.LAST_DEATH_PREVENT.get(), currentTime);

        // Heal the player (like totem)
        player.setHealth(1.0F);

        // Activate spectator mode
        ServerLevel level = serverPlayer.serverLevel();
        activateSpectatorMode(serverPlayer, level, true);

        // Display message
        player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.FREE_SOUL_SAVED)
                        .withStyle(ChatFormatting.GOLD),
                true
        );

        return true;
    }

    /**
     * Activate spectator mode for the player.
     */
    private static void activateSpectatorMode(ServerPlayer player, ServerLevel level, boolean fromDeath) {
        // Store previous game mode and position
        GameType previousGameMode = player.gameMode.getGameModeForPlayer();
        Vec3 originalPosition = player.position();

        // Calculate exit time
        int durationSeconds = AnimusConfig.sigils.freeSoulDuration.get();
        long exitTick = level.getServer().getTickCount() + (durationSeconds * 20L); // Convert seconds to ticks

        // Store spectator state (including original position and whether from death)
        activeSpectators.put(player.getUUID(), new SpectatorState(exitTick, previousGameMode, originalPosition, fromDeath));

        // Set to spectator mode
        player.setGameMode(GameType.SPECTATOR);

        // Display message
        if (!fromDeath) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_ACTIVATED, durationSeconds)
                            .withStyle(ChatFormatting.AQUA),
                    true
            );
        }
    }

    /**
     * Process active spectators - should be called from player tick event.
     */
    public static void tickActiveSpectators(ServerPlayer player, ServerLevel level) {
        UUID playerId = player.getUUID();
        SpectatorState state = activeSpectators.get(playerId);

        if (state == null) {
            return;
        }

        long currentTick = level.getServer().getTickCount();

        // Teleport player back to original position 10 ticks before spectator mode ends
        // This prevents wall teleportation exploits while still allowing scouting
        if (!state.hasTeleportedBack && currentTick >= state.exitTick - 10) {
            player.teleportTo(
                    state.originalPosition.x,
                    state.originalPosition.y,
                    state.originalPosition.z
            );
            state.hasTeleportedBack = true;

            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_RETURNING)
                            .withStyle(ChatFormatting.GOLD),
                    true
            );
        }

        // Check if it's time to exit spectator mode
        if (currentTick >= state.exitTick) {
            // Restore previous game mode
            player.setGameMode(state.previousGameMode);

            // Only heal if this was triggered by death prevention
            if (state.fromDeath) {
                float newHealth = Math.min(player.getHealth() + 10.0F, player.getMaxHealth());
                player.setHealth(newHealth);
            }

            activeSpectators.remove(playerId);

            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_EXPIRED)
                            .withStyle(ChatFormatting.YELLOW),
                    true
            );
        }
    }

    /**
     * Clean up when player logs out.
     */
    public static void onPlayerLogout(ServerPlayer player) {
        UUID playerId = player.getUUID();
        SpectatorState state = activeSpectators.remove(playerId);

        // If player was in spectator mode, restore their game mode
        if (state != null) {
            player.setGameMode(state.previousGameMode);
        }
    }

    /**
     * Check if a player is currently in Free Soul spectator mode.
     */
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
