package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.registry.AnimusRituals;
import com.breakinblocks.neovitae.ritual.Ritual;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class AnimusTestSupport {

    private AnimusTestSupport() {
    }

    /**
     * Every ritual Animus registers, read straight off the registry holders so newly added
     * rituals are covered without touching the tests. Holders that are null (registrations
     * gated behind an optional mod) or unregistered in this environment are skipped.
     */
    public static List<Ritual> animusRituals() {
        List<Ritual> rituals = new ArrayList<>();

        for (Field field : AnimusRituals.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !DeferredHolder.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                DeferredHolder<?, ?> holder = (DeferredHolder<?, ?>) field.get(null);
                if (holder == null) {
                    continue;
                }
                if (holder.get() instanceof Ritual ritual) {
                    rituals.add(ritual);
                }
            } catch (IllegalAccessException | IllegalStateException | NullPointerException ignored) {
                // not registered in this environment
            }
        }

        return rituals;
    }
}
