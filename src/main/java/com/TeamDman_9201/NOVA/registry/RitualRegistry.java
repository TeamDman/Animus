package com.teamdman_9201.nova.registry;

import com.teamdman_9201.nova.rituals.RitualEffectDisenchant;
import com.teamdman_9201.nova.rituals.RitualEffectLuna;
import com.teamdman_9201.nova.rituals.RitualEffectSol;

import net.minecraft.util.StatCollector;

import WayofTime.alchemicalWizardry.api.rituals.Rituals;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualRegistry {
    public static void registerRituals() {
        Rituals.registerRitual("ritualSol", 1, 1000, new RitualEffectSol(), StatCollector
                .translateToLocal("ritual.NOVA.sol"));
        Rituals.registerRitual("ritualLuna", 2, 10000, new RitualEffectLuna(), StatCollector
                .translateToLocal("ritual.NOVA.luna"));
        Rituals.registerRitual("ritualDisenchant", 2, 50000, new RitualEffectDisenchant(),
                StatCollector.translateToLocal("ritual.NOVA.disenchant"));
    }
}
