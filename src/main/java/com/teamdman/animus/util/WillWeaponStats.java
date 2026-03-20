package com.teamdman.animus.util;

/**
 * Shared stat arrays for demon-will powered weapons.
 * Arrays that are identical across multiple sentient weapon classes
 * are centralized here to avoid duplication.
 */
public final class WillWeaponStats {
    private WillWeaponStats() {}

    /** Soul brackets for will level progression (0-4). Used by all sentient weapons. */
    public static final double[] SOUL_BRACKET = new double[]{16, 60, 200, 400, 1000};

    /** Will drop scaling by level. Used by Sentient Bow and Sentient Spear. */
    public static final double[] SOUL_DROP = new double[]{2.0, 4.0, 7.0, 10.0, 15.0};

    /** Static (minimum) will drop by level. Used by Sentient Bow and Sentient Spear. */
    public static final double[] STATIC_DROP = new double[]{1.0, 1.0, 2.0, 3.0, 4.0};

    /** Poison/wither effect amplifier by level. Used by Sentient Bow and Sentient Spear. */
    public static final int[] POISON_LEVEL = new int[]{0, 0, 1, 1, 2};

    /**
     * Determines the will level (0 to {@code SOUL_BRACKET.length}) based on
     * the amount of demon will the player currently holds.
     *
     * @param soulsRemaining the player's current will of the relevant type
     * @return the will level index (0 if below first bracket, up to SOUL_BRACKET.length)
     */
    public static int getLevel(double soulsRemaining) {
        for (int i = 0; i < SOUL_BRACKET.length; i++) {
            if (soulsRemaining < SOUL_BRACKET[i]) {
                return i;
            }
        }
        return SOUL_BRACKET.length;
    }
}
