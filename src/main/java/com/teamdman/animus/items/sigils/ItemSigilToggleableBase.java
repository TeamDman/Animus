package com.teamdman.animus.items.sigils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for toggleable sigils
 * TODO: Implement proper activation state management with Blood Magic API
 * TODO: Add visual indicators for activated state (model overrides)
 */
public abstract class ItemSigilToggleableBase extends ItemSigilBase {
    private static final String ACTIVATED_KEY = "activated";

    public ItemSigilToggleableBase(String name, int lpUsed) {
        super(name, lpUsed);
    }

    /**
     * Check if sigil is activated
     */
    public boolean getActivated(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }
        return tag.getBoolean(ACTIVATED_KEY);
    }

    /**
     * Set activation state
     */
    public void setActivatedState(ItemStack stack, boolean activated) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(ACTIVATED_KEY, activated);
    }

    /**
     * Check if sigil is unusable (e.g., not bound, not enough LP)
     * TODO: Implement proper checks with Blood Magic API
     */
    public boolean isUnusable(ItemStack stack) {
        // TODO: Check binding
        // TODO: Check LP availability
        return false;
    }
}
