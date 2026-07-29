package com.breakinblocks.animusnv.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class InputClientHelper {

    public static boolean isShiftDown() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            return false;
        }
        return InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}
