package com.teamdman.animus.items.sigils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for toggleable sigils
 * TODO: Add visual indicators for activated state (model overrides)
 */
public abstract class ItemSigilToggleableBase extends AnimusSigilBase {
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
     * Check if sigil is unusable (not currently implemented)
     * Individual sigils handle their own binding and LP checks
     */
    public boolean isUnusable(ItemStack stack) {
        return false;
    }
}
