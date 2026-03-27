package com.breakinblocks.animusnv.items.sigils;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.loading.FMLEnvironment;
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
