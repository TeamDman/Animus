package com.teamdman.animus.items.sigils;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Base class for Animus sigils
 * <p>
 * Sigils are single-stack items that consume LP (Life Points) from a bound player's soul network
 * when activated. This base class provides common functionality for all sigil types.
 * <p>
 * TODO: Once Blood Magic 1.20.1 API is studied, extend from Blood Magic's sigil base class
 * and implement proper LP usage, activation, and soul network integration
 *
 * @see wayoftime.bloodmagic.common.item.ISigil Blood Magic's sigil interface (when available)
 */
public abstract class ItemSigilBase extends Item {
    protected final String name;
    protected final int lpUsed;

    /**
     * Creates a new sigil with the specified name and LP cost
     *
     * @param name The internal name of the sigil (used for identification)
     * @param lpUsed The LP cost to activate this sigil
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if lpUsed is negative
     */
    public ItemSigilBase(@NotNull String name, int lpUsed) {
        super(new Item.Properties()
            .stacksTo(1)
        );
        this.name = Objects.requireNonNull(name, "Sigil name cannot be null");
        if (lpUsed < 0) {
            throw new IllegalArgumentException("LP cost cannot be negative: " + lpUsed);
        }
        this.lpUsed = lpUsed;
    }

    /**
     * Get the LP cost of using this sigil
     *
     * @return The amount of LP consumed when activating this sigil
     */
    public int getLpUsed() {
        return lpUsed;
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

    // TODO: Implement Blood Magic integration:
    // - Soul network access
    // - LP consumption
    // - Binding system
    // - Activation toggle for toggleable sigils
}
