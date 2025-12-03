package com.teamdman.animus.client;

import net.minecraft.client.Minecraft;

/**
 * Client-only helper for clipboard operations.
 */
public class ClipboardClientHelper {

    /**
     * Sets the clipboard contents on the client.
     * @param text The text to put in the clipboard
     */
    public static void setClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
