package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.ritual.Ritual;
import wayoftime.bloodmagic.ritual.RitualManager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public final class BloodMagicRitualInjector {

    private BloodMagicRitualInjector() {
    }

    @SuppressWarnings("unchecked")
    public static boolean register(String ritualId, Ritual ritual) {
        try {
            RitualManager manager = BloodMagic.RITUAL_MANAGER;

            Field ritualsField = RitualManager.class.getDeclaredField("rituals");
            ritualsField.setAccessible(true);
            Map<String, Ritual> rituals = (Map<String, Ritual>) ritualsField.get(manager);

            if (rituals.containsKey(ritualId)) {
                return true;
            }

            Field ritualsReverseField = RitualManager.class.getDeclaredField("ritualsReverse");
            ritualsReverseField.setAccessible(true);
            Map<Ritual, String> ritualsReverse = (Map<Ritual, String>) ritualsReverseField.get(manager);

            Field sortedRitualsField = RitualManager.class.getDeclaredField("sortedRituals");
            sortedRitualsField.setAccessible(true);
            List<Ritual> sortedRituals = (List<Ritual>) sortedRitualsField.get(manager);

            rituals.put(ritualId, ritual);
            ritualsReverse.put(ritual, ritualId);

            if (!sortedRituals.isEmpty()) {
                sortedRituals.add(ritual);
            }

            Animus.LOGGER.info("Registered ritual {} with Blood Magic", ritualId);
            return true;
        } catch (Exception e) {
            Animus.LOGGER.error("Failed to register ritual {} with Blood Magic", ritualId, e);
            return false;
        }
    }
}
