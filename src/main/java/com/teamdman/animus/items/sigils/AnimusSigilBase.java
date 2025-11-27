package com.teamdman.animus.items.sigils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilBase;
import wayoftime.bloodmagic.core.data.Binding;

import java.util.List;
import java.util.Objects;

/**
 * Base class for Animus sigils
 * <p>
 * Extends Blood Magic's ItemSigilBase which provides:
 * - IBindable interface (player binding system)
 * - ISigil interface (sigil functionality)
 * - LP cost management
 * - Tooltip formatting
 * <p>
 * Sigils are single-stack items that consume LP from a bound player's soul network when activated.
 */
public abstract class AnimusSigilBase extends ItemSigilBase {
    protected final String name;

    /**
     * Creates a new sigil with the specified name and LP cost
     *
     * @param name The internal name of the sigil (used for identification)
     * @param lpUsed The LP cost to activate this sigil
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if lpUsed is negative
     */
    public AnimusSigilBase(@NotNull String name, int lpUsed) {
        super(name, lpUsed);
        this.name = Objects.requireNonNull(name, "Sigil name cannot be null");
        if (lpUsed < 0) {
            throw new IllegalArgumentException("LP cost cannot be negative: " + lpUsed);
        }
    }

    /**
     * Get the internal name of this sigil
     *
     * @return The sigil's identifier name
     */
    @NotNull
    public String getSigilName() {
        return name;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        // DON'T call super.appendHoverText() - Blood Magic's ItemSigilBase adds bloodmagic namespace tooltips
        // We handle all tooltips in the individual sigil classes using the animus namespace

        // Add binding owner information
        Binding binding = getBinding(stack);
        if (binding != null) {
            // Try to get the owner's name from the client player cache
            String ownerName = "Unknown";
            if (level != null && level.isClientSide()) {
                var minecraft = Minecraft.getInstance();
                var connection = minecraft.getConnection();
                if (connection != null) {
                    var playerInfo = connection.getPlayerInfo(binding.getOwnerId());
                    if (playerInfo != null) {
                        ownerName = playerInfo.getProfile().getName();
                    }
                }
            }

            tooltip.add(Component.literal("Bound to: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(ownerName)
                    .withStyle(ChatFormatting.AQUA)));
        } else {
            tooltip.add(Component.literal("Not bound")
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
