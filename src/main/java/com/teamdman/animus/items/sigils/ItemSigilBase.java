package com.teamdman.animus.items.sigils;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for Animus sigils
 * TODO: Once Blood Magic 1.20.1 API is studied, extend from Blood Magic's sigil base class
 * and implement proper LP usage, activation, and soul network integration
 *
 * For now, this provides a basic structure that can be extended
 */
public abstract class ItemSigilBase extends Item {
    protected final String name;
    protected final int lpUsed;

    public ItemSigilBase(String name, int lpUsed) {
        super(new Item.Properties()
            .stacksTo(1)
        );
        this.name = name;
        this.lpUsed = lpUsed;
    }

    /**
     * Get the LP cost of using this sigil
     */
    public int getLpUsed() {
        return lpUsed;
    }

    /**
     * Get the sigil name
     */
    public String getSigilName() {
        return name;
    }

    // TODO: Implement Blood Magic integration:
    // - Soul network access
    // - LP consumption
    // - Binding system
    // - Activation toggle for toggleable sigils
}
