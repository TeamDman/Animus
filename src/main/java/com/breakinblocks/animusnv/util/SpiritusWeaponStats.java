package com.breakinblocks.animusnv.util;

import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.spiritus.ISpiritus;

public final class SpiritusWeaponStats {
    private SpiritusWeaponStats() {}

    public static final double[] SOUL_BRACKET = new double[]{16, 60, 200, 400, 1000};

    public static final double[] SOUL_DROP = new double[]{2.0, 4.0, 7.0, 10.0, 15.0};

    public static final double[] STATIC_DROP = new double[]{1.0, 1.0, 2.0, 3.0, 4.0};

    public static final int[] POISON_LEVEL = new int[]{0, 0, 1, 1, 2};

    public static int getLevel(double soulsRemaining) {
        for (int i = 0; i < SOUL_BRACKET.length; i++) {
            if (soulsRemaining < SOUL_BRACKET[i]) {
                return i;
            }
        }
        return SOUL_BRACKET.length;
    }

    public static ISpiritus getSoulItem(SpiritusType type) {
        return switch (type) {
            case RUINA -> (ISpiritus) NVItems.MONSTER_SOUL_RUINA.get();
            case NIHILUM -> (ISpiritus) NVItems.MONSTER_SOUL_NIHILUM.get();
            case INVICTUS -> (ISpiritus) NVItems.MONSTER_SOUL_INVICTUS.get();
            case VINDICTA -> (ISpiritus) NVItems.MONSTER_SOUL_VINDICTA.get();
            default -> (ISpiritus) NVItems.MONSTER_SOUL_RAW.get();
        };
    }
}
