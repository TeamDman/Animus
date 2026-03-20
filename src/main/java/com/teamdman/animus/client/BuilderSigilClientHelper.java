package com.teamdman.animus.client;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

public class BuilderSigilClientHelper {
    private static Field rightClickDelayField = null;
    private static boolean reflectionAttempted = false;

    public static void resetRightClickDelay() {
        if (!reflectionAttempted) {
            try {
                Class<?> minecraftClass = Minecraft.class;
                try {
                    rightClickDelayField = minecraftClass.getDeclaredField("rightClickDelay");
                } catch (NoSuchFieldException e) {
                    // Fallback: scan for obfuscated field name
                    for (Field field : minecraftClass.getDeclaredFields()) {
                        if (field.getType() == int.class) {
                            rightClickDelayField = field;
                            break;
                        }
                    }
                }

                if (rightClickDelayField != null) {
                    rightClickDelayField.setAccessible(true);
                }
            } catch (Exception e) {
                // Ignore - disable future attempts
            } finally {
                reflectionAttempted = true;
            }
        }

        if (rightClickDelayField != null) {
            try {
                Minecraft mc = Minecraft.getInstance();
                int currentDelay = rightClickDelayField.getInt(mc);
                if (currentDelay > 0) {
                    rightClickDelayField.setInt(mc, 0);
                }
            } catch (Exception e) {
                // Silently fail
            }
        }
    }
}
