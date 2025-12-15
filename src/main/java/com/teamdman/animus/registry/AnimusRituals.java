package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.rituals.*;
import com.teamdman.animus.rituals.imperfect.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.ritual.ImperfectRitual;
import wayoftime.bloodmagic.ritual.Ritual;
import wayoftime.bloodmagic.ritual.RitualRegistry;

import java.util.function.Supplier;

/**
 * Registry for Animus rituals.
 * In Blood Magic 4.x for 1.21.1, rituals are registered via DeferredRegister.
 *
 * Note: We use Blood Magic's internal registry keys (RitualRegistry.RITUAL_REGISTRY_KEY
 * and IMPERFECT_RITUAL_REGISTRY_KEY) rather than the API keys because the API keys
 * are typed for interfaces (IRitual, IImperfectRitual) while our ritual classes
 * extend the concrete implementation classes (Ritual, ImperfectRitual).
 */
public class AnimusRituals {

    // Regular rituals - using Blood Magic's internal registry key for concrete Ritual type
    public static final DeferredRegister<Ritual> RITUALS =
        DeferredRegister.create(RitualRegistry.RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    // Imperfect rituals - using our own DeferredRegister with Blood Magic's internal registry key
    public static final DeferredRegister<ImperfectRitual> IMPERFECT_RITUALS =
        DeferredRegister.create(RitualRegistry.IMPERFECT_RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

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
    // Registered using our own DeferredRegister rather than Blood Magic's shared one

    public static final DeferredHolder<ImperfectRitual, RitualHunger> IMPERFECT_HUNGER =
        registerImperfectRitual(Constants.Rituals.HUNGER, RitualHunger::new);

    public static final DeferredHolder<ImperfectRitual, RitualRegression> IMPERFECT_REGRESSION =
        registerImperfectRitual(Constants.Rituals.REGRESSION, RitualRegression::new);

    public static final DeferredHolder<ImperfectRitual, RitualEnhancement> IMPERFECT_ENHANCEMENT =
        registerImperfectRitual(Constants.Rituals.ENHANCEMENT, RitualEnhancement::new);

    public static final DeferredHolder<ImperfectRitual, RitualReduction> IMPERFECT_REDUCTION =
        registerImperfectRitual(Constants.Rituals.REDUCTION, RitualReduction::new);

    public static final DeferredHolder<ImperfectRitual, RitualBoundlessSkies> IMPERFECT_BOUNDLESS_SKIES =
        registerImperfectRitual(Constants.Rituals.BOUNDLESS_SKIES, RitualBoundlessSkies::new);

    public static final DeferredHolder<ImperfectRitual, RitualClearSkies> IMPERFECT_CLEAR_SKIES =
        registerImperfectRitual(Constants.Rituals.CLEAR_SKIES, RitualClearSkies::new);

    public static final DeferredHolder<ImperfectRitual, RitualNeptuneBlessing> IMPERFECT_NEPTUNE_BLESSING =
        registerImperfectRitual(Constants.Rituals.NEPTUNE_BLESSING, RitualNeptuneBlessing::new);

    public static final DeferredHolder<ImperfectRitual, RitualWarden> IMPERFECT_WARDEN =
        registerImperfectRitual(Constants.Rituals.WARDEN, RitualWarden::new);

    /**
     * Helper method to register imperfect rituals using our own DeferredRegister
     */
    private static <T extends ImperfectRitual> DeferredHolder<ImperfectRitual, T> registerImperfectRitual(
            String name, Supplier<T> supplier) {
        return IMPERFECT_RITUALS.register(name, supplier);
    }

    /**
     * Force class loading to trigger static field initialization.
     * Called during mod construction to ensure rituals are registered.
     */
    public static void init() {
        // This method intentionally left empty.
        // Simply calling this method forces the class to be loaded,
        // which triggers all static field initializers (the DeferredHolder registrations).
    }
}
