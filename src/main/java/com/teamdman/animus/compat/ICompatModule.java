package com.teamdman.animus.compat;

/**
 * Interface for compatibility modules
 * Each mod integration should implement this interface
 */
public interface ICompatModule {
    /**
     * Initialize the compatibility module
     * This is called during mod setup if the target mod is loaded
     */
    void init();

    /**
     * Get the mod ID this module provides compatibility for
     */
    String getModId();
}
