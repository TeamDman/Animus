package com.teamdman.animus.client;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;

/**
 * Client-only helper for Sigil of the Phantom Builder.
 * Contains the right-click delay reset logic that requires Minecraft class access.
 */
public class BuilderSigilClientHelper {
    private static Field rightClickDelayField = null;
    private static boolean reflectionAttempted = false;

    /**
     * Resets right-click delay for fast building.
     * Uses reflection to access Minecraft's internal rightClickDelay field.
     */
    public static void resetRightClickDelay() {
        if (!reflectionAttempted) {
            try {
                Class<?> minecraftClass = Minecraft.class;
                try {
                    rightClickDelayField = minecraftClass.getDeclaredField("rightClickDelay");
                } catch (NoSuchFieldException e) {
                    // Try obfuscated name patterns
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
                // Reflection failed, disable future attempts
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
