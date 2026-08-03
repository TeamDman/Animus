package com.teamdman.animus.gametest;

import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.ritual.Ritual;

import java.util.List;

public final class AnimusTestSupport {

    private AnimusTestSupport() {
    }

    /**
     * Every ritual Animus contributes to Blood Magic's registry, including the ones injected by
     * the compat modules, so newly added rituals are covered without touching the tests.
     */
    public static List<Ritual> animusRituals() {
        return BloodMagic.RITUAL_MANAGER.getRituals().stream()
            .filter(ritual -> ritual.getClass().getName().startsWith("com.teamdman.animus"))
            .toList();
    }
}
