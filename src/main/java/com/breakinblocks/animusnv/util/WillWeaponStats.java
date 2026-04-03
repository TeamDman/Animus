package com.breakinblocks.animusnv.util;

import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.will.ISpiritus;

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
     * the amount of Spiritus the player currently holds.
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

    /**
     * Maps a SpiritusType to the corresponding monster soul item.
     */
    public static ISpiritus getSoulItem(SpiritusType type) {
        return switch (type) {
            case CORROSIVE -> (ISpiritus) NVItems.MONSTER_SOUL_CORROSIVE.get();
            case DESTRUCTIVE -> (ISpiritus) NVItems.MONSTER_SOUL_DESTRUCTIVE.get();
            case STEADFAST -> (ISpiritus) NVItems.MONSTER_SOUL_STEADFAST.get();
            case VENGEFUL -> (ISpiritus) NVItems.MONSTER_SOUL_VENGEFUL.get();
            default -> (ISpiritus) NVItems.MONSTER_SOUL_RAW.get();
        };
    }
}
