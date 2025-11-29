package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.*;

/**
 * Sigil of the Free Soul - Grants temporary spectator mode and prevents death
 * <p>
 * Features:
 * - Right-click to manually enter spectator mode for configurable duration (default: 10 seconds)
 * - Costs configurable LP on use (default: 5000 LP)
 * - Automatically activates on death (like totem of undying) if player has enough LP
 * - Cannot trigger death prevention again for configurable cooldown (default: 60 seconds)
 * - Must be bound to use
 * - Teleports player back to original position 10 ticks before spectator mode ends (prevents wall exploits)
 * - Heals player 10 health (5 hearts) when returning to previous game mode
 * <p>
 * Death prevention hooked up in AnimusEventHandler.onLivingDeath()
 * Spectator mode timer hooked up in AnimusEventHandler.onPlayerTick()
 */
public class ItemSigilFreeSoul extends AnimusSigilBase {

    // Track players currently in spectator mode - map of player UUID to exit time
    private static final Map<UUID, SpectatorState> activeSpectators = new HashMap<>();

    private static class SpectatorState {
        final long exitTick;
        final GameType previousGameMode;
        final Vec3 originalPosition;
        boolean hasTeleportedBack = false;

        SpectatorState(long exitTick, GameType previousGameMode, Vec3 originalPosition) {
            this.exitTick = exitTick;
            this.previousGameMode = previousGameMode;
            this.originalPosition = originalPosition;
        }
    }

    public ItemSigilFreeSoul() {
        super(Constants.Sigils.FREE_SOUL, 0); // LP cost handled in activation
    }

    /**
     * Manual activation - enter spectator mode
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Check binding
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

        // Check if player is already in spectator mode
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_ALREADY_SPECTATOR)
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return InteractionResultHolder.fail(stack);
            }

            // Get LP cost from config
            int lpCost = AnimusConfig.sigils.freeSoulLPCost.get();

            // Try to consume LP
            SoulNetwork network = NetworkHelper.getSoulNetwork(player);
            SoulTicket ticket = new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_FREE_SOUL),
                lpCost
            );

            var syphonResult = network.syphonAndDamage(player, ticket);
            if (!syphonResult.isSuccess()) {
                // Not enough LP
                player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.FREE_SOUL_NO_LP)
                        .withStyle(ChatFormatting.RED),
                    true
                );
                return InteractionResultHolder.fail(stack);
            }

            // Activate spectator mode
            activateSpectatorMode(serverPlayer, (ServerLevel) level, false);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    /**
     * Try to prevent death by activating spectator mode
     * Returns true if death was prevented, false otherwise
     */
    public static boolean tryPreventDeath(Player player, DamageSource source) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        // Find Free Soul sigil in inventory
        ItemStack freeSoulStack = null;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemSigilFreeSoul) {
                freeSoulStack = stack;
                break;
            }
        }

        if (freeSoulStack == null) {
            return false;
        }

        // Check if sigil is bound to player
        ItemSigilFreeSoul sigil = (ItemSigilFreeSoul) freeSoulStack.getItem();
        var binding = sigil.getBinding(freeSoulStack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return false;
        }

        // Check cooldown
        long lastTrigger = freeSoulStack.getOrCreateTag().getLong("LastDeathPrevent");
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
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        int currentEssence = network.getCurrentEssence();

        if (currentEssence < lpCost) {
            // Not enough LP
            return false;
        }

        // Consume LP
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_FREE_SOUL),
            lpCost
        );
        var syphonResult = network.syphonAndDamage(player, ticket);
        if (!syphonResult.isSuccess()) {
            return false;
        }

        // Set cooldown
        freeSoulStack.getOrCreateTag().putLong("LastDeathPrevent", currentTime);

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
     * Activate spectator mode for the player
     */
    private static void activateSpectatorMode(ServerPlayer player, ServerLevel level, boolean fromDeath) {
        // Store previous game mode and position
        GameType previousGameMode = player.gameMode.getGameModeForPlayer();
        Vec3 originalPosition = player.position();

        // Calculate exit time
        int durationSeconds = AnimusConfig.sigils.freeSoulDuration.get();
        long exitTick = level.getServer().getTickCount() + (durationSeconds * 20); // Convert seconds to ticks

        // Store spectator state (including original position)
        activeSpectators.put(player.getUUID(), new SpectatorState(exitTick, previousGameMode, originalPosition));

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
     * Process active spectators - should be called from player tick event
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

            // Heal the player 10 health (5 hearts) when returning
            float newHealth = Math.min(player.getHealth() + 10.0F, player.getMaxHealth());
            player.setHealth(newHealth);

            activeSpectators.remove(playerId);

            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.FREE_SOUL_EXPIRED)
                    .withStyle(ChatFormatting.YELLOW),
                true
            );
        }
    }

    /**
     * Clean up when player logs out
     */
    public static void onPlayerLogout(ServerPlayer player) {
        UUID playerId = player.getUUID();
        SpectatorState state = activeSpectators.remove(playerId);

        // If player was in spectator mode, restore their game mode
        if (state != null) {
            player.setGameMode(state.previousGameMode);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_FREE_SOUL_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_FREE_SOUL_INFO));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_FREE_SOUL_COST,
            AnimusConfig.sigils.freeSoulLPCost.get()));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_FREE_SOUL_DURATION,
            AnimusConfig.sigils.freeSoulDuration.get()));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_FREE_SOUL_DEATH));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_FREE_SOUL_COOLDOWN,
            AnimusConfig.sigils.freeSoulCooldown.get()));

        // Show cooldown if exists
        if (stack.hasTag()) {
            long lastTrigger = stack.getTag().getLong("LastDeathPrevent");
            if (lastTrigger > 0) {
                long currentTime = System.currentTimeMillis();
                int cooldownSeconds = AnimusConfig.sigils.freeSoulCooldown.get();
                long cooldownMillis = cooldownSeconds * 1000L;
                long remaining = cooldownMillis - (currentTime - lastTrigger);

                if (remaining > 0) {
                    long remainingSeconds = remaining / 1000;
                    tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_FREE_SOUL_COOLDOWN_REMAINING,
                        remainingSeconds).withStyle(ChatFormatting.RED));
                }
            }
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }
}
