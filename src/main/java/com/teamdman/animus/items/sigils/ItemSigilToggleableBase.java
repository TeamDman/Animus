package com.teamdman.animus.items.sigils;

import com.teamdman.animus.registry.AnimusDataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for toggleable sigils
 * Visual indicators are provided via model overrides (registered in AnimusClientSetup)
 */
public abstract class ItemSigilToggleableBase extends AnimusSigilBase {

    public ItemSigilToggleableBase(String name, int lpUsed) {
        super(name, lpUsed);
    }

    /**
     * Check if sigil is activated
     */
    public boolean getActivated(ItemStack stack) {
        Boolean activated = stack.get(AnimusDataComponents.SIGIL_ACTIVATED.get());
        return activated != null && activated;
    }

    /**
     * Set activation state
     */
    public void setActivatedState(ItemStack stack, boolean activated) {
        stack.set(AnimusDataComponents.SIGIL_ACTIVATED.get(), activated);
    }

    /**
     * Check if sigil is unusable (not currently implemented)
     * Individual sigils handle their own binding and LP checks
     */
    public boolean isUnusable(ItemStack stack) {
        return false;
    }
}
