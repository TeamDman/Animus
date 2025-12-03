package com.teamdman.animus.client;

import net.minecraft.client.gui.screens.Screen;

/**
 * Client-only helper for input checking.
 */
public class InputClientHelper {

    /**
     * Checks if shift is being held down on the client.
     * @return true if shift is held
     */
    public static boolean isShiftDown() {
        return Screen.hasShiftDown();
    }
}
