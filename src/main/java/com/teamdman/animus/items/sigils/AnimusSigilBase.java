package com.teamdman.animus.items.sigils;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import wayoftime.bloodmagic.common.item.sigil.ItemSigilBase;

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
}
