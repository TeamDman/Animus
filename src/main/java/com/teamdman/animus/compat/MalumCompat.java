package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.malum.RitualSoulStainedBlood;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.ritual.ImperfectRitual;
import wayoftime.bloodmagic.ritual.RitualRegistry;

/**
 * Compatibility module for Malum
 * Handles spirit harvesting integration for weapons
 *
 * Features:
 * - Spirit harvesting for sentient weapons
 * - Enchantment compatibility checks
 * - Imperfect Ritual of the Soul-Stained Blood
 */
public class MalumCompat implements ICompatModule {

    private static MalumCompat INSTANCE;

    // DeferredRegister for Malum compatibility imperfect rituals
    public static final DeferredRegister<ImperfectRitual> IMPERFECT_RITUALS =
        DeferredRegister.create(RitualRegistry.IMPERFECT_RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    // ===== Imperfect Rituals =====

    // Imperfect Ritual of the Soul-Stained Blood - grants Sacrificial Empowerment effect
    // Requires Block of Hallowed Gold on Imperfect Ritual Stone
    public static final DeferredHolder<ImperfectRitual, RitualSoulStainedBlood> SOUL_STAINED_BLOOD =
        IMPERFECT_RITUALS.register(Constants.Rituals.SOUL_STAINED_BLOOD, () -> new RitualSoulStainedBlood());

    public MalumCompat() {
        INSTANCE = this;
    }

    public static MalumCompat getInstance() {
        return INSTANCE;
    }

    /**
     * Register the DeferredRegisters to the mod event bus
     * This must be called early, during mod construction
     */
    public static void registerDeferred(IEventBus modEventBus) {
        IMPERFECT_RITUALS.register(modEventBus);
        Animus.LOGGER.info("Registered Malum compatibility registries (imperfect rituals)");
    }

    @Override
    public void init() {
        Animus.LOGGER.info("Initializing Malum compatibility");

        // Malum integration is passive - no event listeners needed
        // Integration happens through direct API calls from weapon items

        Animus.LOGGER.info("Malum compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "malum";
    }
}
