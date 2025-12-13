package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import wayoftime.bloodmagic.util.SoulTicket;

import java.util.*;

/**
 * Sigil of Remedium - Continuously cleanses negative status effects
 * <p>
 * Features:
 * - Right-click to toggle active/inactive
 * - While active, removes all negative effects once per second
 * - Costs 50 LP per effect removed
 * - Must be bound to use
 * <p>
 * Tick handler hooked up in AnimusEventHandler.onPlayerTick()
 */
public class ItemSigilRemedium extends AnimusSigilBase {

    // Track active sigils per player - map of player UUID to (last tick time, ItemStack)
    private static final Map<UUID, ActiveSigil> activeSigils = new HashMap<>();

    private static class ActiveSigil {
        final ItemStack stack;
        long lastCleanTick;

        ActiveSigil(ItemStack stack, long lastCleanTick) {
            this.stack = stack;
            this.lastCleanTick = lastCleanTick;
        }
    }

    public ItemSigilRemedium() {
        super(Constants.Sigils.REMEDIUM, 0); // No cost on toggle, only on effect removal
    }

    /**
     * Toggle the sigil active state
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Check binding
        var binding = getBinding(stack);
        if (binding == null || binding.isEmpty() || !binding.uuid().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

        // Toggle active state
        boolean currentState = isActive(stack);
        setActive(stack, !currentState);

        // Update tracking
        if (!currentState) {
            // Activating
            if (level instanceof ServerLevel serverLevel) {
                activeSigils.put(player.getUUID(), new ActiveSigil(stack, serverLevel.getServer().getTickCount()));
            }
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.REMEDIUM_ACTIVATED)
                    .withStyle(ChatFormatting.GREEN),
                true
            );
        } else {
            // Deactivating
            activeSigils.remove(player.getUUID());
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.REMEDIUM_DEACTIVATED)
                    .withStyle(ChatFormatting.RED),
                true
            );
        }

        return InteractionResultHolder.success(stack);
    }

    /**
     * Check if this sigil is active
     */
    public static boolean isActive(ItemStack stack) {
        Boolean active = stack.get(AnimusDataComponents.SIGIL_ACTIVATED.get());
        return active != null && active;
    }

    /**
     * Set the active state of this sigil
     */
    private void setActive(ItemStack stack, boolean active) {
        stack.set(AnimusDataComponents.SIGIL_ACTIVATED.get(), active);
    }

    /**
     * Process active sigils - should be called from player tick event
     */
    public static void tickActiveSigils(Player player, ServerLevel level) {
        UUID playerId = player.getUUID();
        ActiveSigil activeSigil = activeSigils.get(playerId);

        if (activeSigil == null) {
            return;
        }

        // Verify the player still has the active sigil
        boolean hasActiveSigil = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof ItemSigilRemedium && isActive(stack)) {
                hasActiveSigil = true;
                stack.set(AnimusDataComponents.SIGIL_ACTIVATED.get(), true); // Ensure sync
                break;
            }
        }

        if (!hasActiveSigil) {
            // Player no longer has active sigil
            activeSigils.remove(playerId);
            return;
        }

        // Check if it's time to cleanse (once per second = 20 ticks)
        long currentTick = level.getServer().getTickCount();
        if (currentTick - activeSigil.lastCleanTick < 20) {
            return;
        }

        // Time to cleanse!
        activeSigil.lastCleanTick = currentTick;

        // Collect all negative effects
        List<Holder<MobEffect>> negativeEffects = new ArrayList<>();
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            Holder<MobEffect> effect = effectInstance.getEffect();
            // Check if the effect is harmful (negative)
            if (!effect.value().isBeneficial()) {
                negativeEffects.add(effect);
            }
        }

        // If no negative effects, nothing to do
        if (negativeEffects.isEmpty()) {
            return;
        }

        // Calculate LP cost (50 per effect)
        int lpCost = negativeEffects.size() * 50;

        // Try to consume LP from soul network
        wayoftime.bloodmagic.common.datacomponent.SoulNetwork network = wayoftime.bloodmagic.util.helper.SoulNetworkHelper.getSoulNetwork(player);
        SoulTicket ticket = SoulTicket.create(lpCost);

        var syphonResult = network.syphonAndDamage(player, ticket);
        if (!syphonResult.isSuccess()) {
            // Not enough LP - deactivate sigil
            setActiveStatic(activeSigil.stack, false);
            activeSigils.remove(playerId);
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.REMEDIUM_NO_LP)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Remove all negative effects
        for (Holder<MobEffect> effect : negativeEffects) {
            player.removeEffect(effect);
        }
    }

    /**
     * Static version of setActive for use in tick handler
     */
    private static void setActiveStatic(ItemStack stack, boolean active) {
        stack.set(AnimusDataComponents.SIGIL_ACTIVATED.get(), active);
    }

    /**
     * Clean up when player logs out
     */
    public static void onPlayerLogout(UUID playerId) {
        activeSigils.remove(playerId);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REMEDIUM_FLAVOUR));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REMEDIUM_INFO));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REMEDIUM_COST));

        if (isActive(stack)) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REMEDIUM_ACTIVE)
                .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_REMEDIUM_INACTIVE)
                .withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }
}
