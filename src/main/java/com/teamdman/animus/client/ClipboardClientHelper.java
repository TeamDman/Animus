package com.teamdman.animus.client;

import net.minecraft.client.Minecraft;

public class ClipboardClientHelper {

    public static void setClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
