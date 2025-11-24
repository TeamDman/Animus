package com.teamdman.animus.items;

import net.minecraft.world.item.Item;

/**
 * Base item class for common functionality
 */
public class ItemBase extends Item {
    public ItemBase(Properties properties) {
        super(properties);
    }

    public ItemBase() {
        this(new Properties());
    }
}
