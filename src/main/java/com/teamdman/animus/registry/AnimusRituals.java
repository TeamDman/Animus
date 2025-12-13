package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.rituals.*;
import com.teamdman.animus.rituals.imperfect.RitualHunger;
import com.teamdman.animus.rituals.imperfect.RitualRegression;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.ritual.ImperfectRitual;
import wayoftime.bloodmagic.ritual.Ritual;
import wayoftime.bloodmagic.ritual.RitualRegistry;

import java.util.function.Supplier;

/**
 * Registry for Animus rituals.
 * In Blood Magic 4.x for 1.21.1, rituals are registered via DeferredRegister.
 */
public class AnimusRituals {

    // Regular rituals
    public static final DeferredRegister<Ritual> RITUALS =
        DeferredRegister.create(RitualRegistry.RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    // === Regular Rituals ===

    public static final DeferredHolder<Ritual, RitualCulling> CULLING =
        RITUALS.register(Constants.Rituals.CULLING, RitualCulling::new);

    public static final DeferredHolder<Ritual, RitualEndlessGreed> ENDLESS_GREED =
        RITUALS.register(Constants.Rituals.ENDLESS_GREED, RitualEndlessGreed::new);

    public static final DeferredHolder<Ritual, RitualEntropy> ENTROPY =
        RITUALS.register(Constants.Rituals.ENTROPY, RitualEntropy::new);

    public static final DeferredHolder<Ritual, RitualLuna> LUNA =
        RITUALS.register(Constants.Rituals.LUNA, RitualLuna::new);

    public static final DeferredHolder<Ritual, RitualNaturesLeach> NATURES_LEACH =
        RITUALS.register(Constants.Rituals.LEACH, RitualNaturesLeach::new);

    public static final DeferredHolder<Ritual, RitualNoliteIgnem> NOLITE_IGNEM =
        RITUALS.register(Constants.Rituals.NOLITE_IGNEM, RitualNoliteIgnem::new);

    public static final DeferredHolder<Ritual, RitualPeacefulBeckoning> PEACEFUL_BECKONING =
        RITUALS.register(Constants.Rituals.PEACEFUL_BECKONING, RitualPeacefulBeckoning::new);

    public static final DeferredHolder<Ritual, RitualPersistence> PERSISTENCE =
        RITUALS.register(Constants.Rituals.PERSISTENCE, RitualPersistence::new);

    public static final DeferredHolder<Ritual, RitualRelentlessTides> RELENTLESS_TIDES =
        RITUALS.register(Constants.Rituals.RELENTLESS_TIDES, RitualRelentlessTides::new);

    public static final DeferredHolder<Ritual, RitualReparare> REPARARE =
        RITUALS.register(Constants.Rituals.REPARARE, RitualReparare::new);

    public static final DeferredHolder<Ritual, RitualSerenity> SERENITY =
        RITUALS.register(Constants.Rituals.SERENITY, RitualSerenity::new);

    public static final DeferredHolder<Ritual, RitualSiphon> SIPHON =
        RITUALS.register(Constants.Rituals.SIPHON, RitualSiphon::new);

    public static final DeferredHolder<Ritual, RitualSol> SOL =
        RITUALS.register(Constants.Rituals.SOL, RitualSol::new);

    public static final DeferredHolder<Ritual, RitualSourceVitaeum> SOURCE_VITAEUM =
        RITUALS.register(Constants.Rituals.SOURCE_VITAEUM, RitualSourceVitaeum::new);

    public static final DeferredHolder<Ritual, RitualSteadfastHeart> STEADFAST_HEART =
        RITUALS.register(Constants.Rituals.STEADFAST, RitualSteadfastHeart::new);

    public static final DeferredHolder<Ritual, RitualUnmaking> UNMAKING =
        RITUALS.register(Constants.Rituals.UNMAKING, RitualUnmaking::new);

    // === Imperfect Rituals ===
    // Registered using Blood Magic's imperfect ritual registry

    public static final DeferredHolder<ImperfectRitual, RitualHunger> IMPERFECT_HUNGER =
        registerImperfectRitual(Constants.Rituals.HUNGER, RitualHunger::new);

    public static final DeferredHolder<ImperfectRitual, RitualRegression> IMPERFECT_REGRESSION =
        registerImperfectRitual(Constants.Rituals.REGRESSION, RitualRegression::new);

    /**
     * Helper method to register imperfect rituals using Blood Magic's registry
     */
    private static <T extends ImperfectRitual> DeferredHolder<ImperfectRitual, T> registerImperfectRitual(
            String name, Supplier<T> supplier) {
        return RitualRegistry.IMPERFECT_RITUALS.register(name, supplier);
    }
}
