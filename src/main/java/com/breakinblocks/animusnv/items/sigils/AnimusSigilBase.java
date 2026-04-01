package com.breakinblocks.animusnv.items.sigils;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.teams.FTBTeamsBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import com.breakinblocks.neovitae.common.item.sigil.ItemSigilBase;
import com.breakinblocks.neovitae.common.datacomponent.Binding;

import java.util.List;
import java.util.Objects;

/**
 * Base class for Animus sigils
 * <p>
 * Extends NeoVitae's ItemSigilBase which provides:
 * - IBindable interface (player binding system)
 * - ISigil interface (sigil functionality)
 * - EV cost management
 * - Tooltip formatting
 * <p>
 * Sigils are single-stack items that consume EV from a bound player's Anima when activated.
 */
public abstract class AnimusSigilBase extends ItemSigilBase {
    protected final String name;

    public AnimusSigilBase(@NotNull String name, int lpUsed) {
        super(name, lpUsed);
        this.name = Objects.requireNonNull(name, "Sigil name cannot be null");
        if (lpUsed < 0) {
            throw new IllegalArgumentException("EV cost cannot be negative: " + lpUsed);
        }
    }

    @NotNull
    public String getSigilName() {
        return name;
    }

    /**
     * Check if the given binding belongs to the player, either directly (player UUID match)
     * or via team membership (binding owner is a team UUID and the player is on that team).
     *
     * @param binding The binding to check (may be null or empty)
     * @param player The player attempting to use the item
     * @return true if the player owns the binding or is on the bound team
     */
    protected static boolean isBindingOwner(Binding binding, Player player) {
        if (binding == null || binding.isEmpty()) return false;
        if (binding.uuid().equals(player.getUUID())) return true;
        // Check if the binding is a team binding and the player is on that team
        if (player instanceof ServerPlayer serverPlayer && ModList.get().isLoaded("ftbteams")) {
            try {
                return FTBTeamsBindingHelper.isPlayerOnTeam(serverPlayer, binding.uuid());
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Don't call super - NeoVitae's ItemSigilBase adds neovitae namespace tooltips

        Binding binding = getBinding(stack);
        if (binding != null && !binding.isEmpty()) {
            String ownerName = binding.name();
            if (ownerName == null || ownerName.isEmpty()) {
                ownerName = "Unknown";
            }

            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.BOUND_TO)
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(ownerName)
                    .withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.NOT_BOUND)
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
