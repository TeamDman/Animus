package com.breakinblocks.animusnv.client;

import net.minecraft.client.Minecraft;

public class ClipboardClientHelper {

    public static void setClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }
}
