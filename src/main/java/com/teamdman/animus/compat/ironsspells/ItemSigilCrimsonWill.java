package com.teamdman.animus.compat.ironsspells;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.items.sigils.AnimusSigilBase;
import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Sigil of Crimson Will - Empowers spell casting with demon will and LP
 *
 * Features:
 * - Toggle active/inactive with right-click
 * - While active: Boosts spell power and summon power by 30-50%
 * - Base 30% bonus, scales up to 50% with demon will (0-4096 will)
 * - Consumes LP per spell cast (scales with spell cost)
 * - Consumes demon will per spell cast
 *
 * Handler in CrimsonWillSpellHandler.java
 */
public class ItemSigilCrimsonWill extends AnimusSigilBase {

    public ItemSigilCrimsonWill() {
        super("crimson_will", 0); // No cost on toggle, only on spell cast
    }

    public static boolean isActive(ItemStack stack) {
        return stack.getOrDefault(AnimusDataComponents.SIGIL_ACTIVATED.get(), false);
    }

    public static void setActive(ItemStack stack, boolean active) {
        stack.set(AnimusDataComponents.SIGIL_ACTIVATED.get(), active);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        var binding = getBinding(stack);
        if (binding == null || binding.isEmpty() || !binding.uuid().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

        boolean currentState = isActive(stack);
        setActive(stack, !currentState);

        if (!currentState) {
            player.displayClientMessage(
                Component.literal("Sigil of Crimson Will: Active")
                    .withStyle(ChatFormatting.DARK_RED),
                true
            );
        } else {
            player.displayClientMessage(
                Component.literal("Sigil of Crimson Will: Inactive")
                    .withStyle(ChatFormatting.GRAY),
                true
            );
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        tooltip.add(Component.literal("Empowers spells with demon will and blood")
            .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Power Boost:")
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  • Base: +30% spell & summon damage")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("  • Scales up to +50% (at 4096 demon will)")
            .withStyle(ChatFormatting.GRAY));

        // Client-side boost tooltip removed since DistExecutor is gone

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Cost per spell cast:")
            .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("  • " + AnimusConfig.ironsSpells.crimsonWillLPPerMana.get() + " LP per mana point")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal(""));

        // Show active/inactive status
        if (isActive(stack)) {
            tooltip.add(Component.literal("Status: Active")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        } else {
            tooltip.add(Component.literal("Status: Inactive")
                .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Right-click to toggle active/inactive")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }
}
