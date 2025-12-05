package com.teamdman.animus.items.sigils;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusAttributes;
import com.teamdman.animus.registry.AnimusSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import wayoftime.bloodmagic.core.data.Binding;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import net.minecraft.core.NonNullList;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilHolding;

import java.util.List;
import java.util.UUID;

/**
 * Sigil of the Monk - A curio sigil that enhances unarmed combat
 *
 * When worn and activated:
 * - Provides +10 unarmed damage (configurable)
 * - Grants diamond-level mining speed when mining with empty hands
 * - Consumes 5 LP per second (100 LP per tick / 20 ticks = 5 LP/s)
 *
 * Can be equipped in any curio slot
 */
public class ItemSigilMonk extends ItemSigilToggleableBase implements ICurioItem {
    private static final UUID UNARMED_DAMAGE_UUID = UUID.fromString("b3f47e6a-1c2d-4f8e-9a0b-5c6d7e8f9a0b");

    // LP cost: 5 LP per second = 100 LP per 20 ticks
    // We check every 20 ticks (1 second) and consume 100 LP
    private static final int LP_COST_PER_SECOND = 100;

    public ItemSigilMonk() {
        super(Constants.Sigils.MONK, 0); // No LP cost for activation, only passive drain
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide || isUnusable(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        // Check binding
        Binding binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

        // Toggle activation
        boolean wasActivated = getActivated(stack);
        setActivatedState(stack, !wasActivated);

        // Play ninja_time sound at 50% volume when activating
        if (!wasActivated) {
            level.playSound(
                null, // null = play for all nearby players
                player.getX(),
                player.getY(),
                player.getZ(),
                AnimusSounds.NINJA_TIME.get(),
                SoundSource.PLAYERS,
                0.5f, // 50% volume
                1.0f  // normal pitch
            );
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // Return false to prevent shift-right-click from equipping instead of toggling
        // Player must manually place the sigil in the curio slot
        return false;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        // No passive LP drain - LP is consumed per action (hit/mine)
    }

    /**
     * Consume LP from the player's soul network for a Sigil of the Demon Monk action
     * @param player The player using the sigil
     * @param amount The amount of LP to consume
     * @return true if LP was successfully consumed, false if not enough LP
     */
    public static boolean consumeLP(Player player, int amount) {
        if (player.level().isClientSide()) {
            return false;
        }

        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        if (network == null) {
            return false;
        }

        if (network.getCurrentEssence() < amount) {
            return false;
        }

        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_MONK),
            amount
        );
        network.syphon(ticket, false);
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();

        // Only apply modifiers if activated and bound
        if (getActivated(stack) && getBinding(stack) != null) {
            double unarmedDamage = AnimusConfig.sigils.monkUnarmedDamage.get();
            modifiers.put(
                AnimusAttributes.UNARMED_DAMAGE.get(),
                new AttributeModifier(UNARMED_DAMAGE_UUID, "Sigil of the Monk unarmed damage", unarmedDamage, AttributeModifier.Operation.ADDITION)
            );
        }

        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_MONK_FLAVOUR)
            .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_MONK_INFO)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_MONK_DAMAGE, AnimusConfig.sigils.monkUnarmedDamage.get())
            .withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_MONK_MINING)
            .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_MONK_COST)
            .withStyle(ChatFormatting.DARK_RED));

        // Show activation state
        if (getActivated(stack)) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_MONK_ACTIVE)
                .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_MONK_INACTIVE)
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /**
     * Check if a player has an active Sigil of the Demon Monk equipped (in curios, inventory, or Sigil of Holding)
     */
    public static boolean hasActiveSigil(Player player) {
        // Check curios slots
        boolean inCurios = top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
            .map(inv -> inv.findFirstCurio(stack -> isActiveSigilMonk(stack)).isPresent())
            .orElse(false);

        if (inCurios) {
            return true;
        }

        // Check player inventory (including Sigil of Holding)
        for (ItemStack stack : player.getInventory().items) {
            if (isActiveSigilMonk(stack)) {
                return true;
            }

            // Check inside Sigil of Holding
            if (stack.getItem() instanceof ItemSigilHolding) {
                NonNullList<ItemStack> holdingInv = ItemSigilHolding.getInternalInventory(stack);
                for (ItemStack heldStack : holdingInv) {
                    if (isActiveSigilMonk(heldStack)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check if an ItemStack is an active Sigil of the Demon Monk
     */
    private static boolean isActiveSigilMonk(ItemStack stack) {
        if (stack.getItem() instanceof ItemSigilMonk sigil) {
            return sigil.getActivated(stack) && sigil.getBinding(stack) != null;
        }
        return false;
    }
}
