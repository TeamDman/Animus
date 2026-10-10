package com.breakinblocks.animusnv.compat.ironsartifice;

import com.breakinblocks.animusnv.Constants;
import io.redspace.irons_artifice.data.ComponentType;
import io.redspace.irons_artifice.data.Value;

public final class AnimusShotComponents {
    public static final ComponentType<Value> BLOOD_BULLET =
            new ComponentType<>(Constants.rl("blood_bullet"), () -> Value.of(0));
    public static final ComponentType<Value> SPIRIT_POWDER =
            new ComponentType<>(Constants.rl("spirit_powder"), () -> Value.of(0));

    private AnimusShotComponents() {}
}
