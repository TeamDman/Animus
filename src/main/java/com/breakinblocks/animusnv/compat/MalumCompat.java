package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.compat.malum.RitualSoulStainedBlood;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;
import com.breakinblocks.neovitae.ritual.RitualRegistry;

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

    public static final DeferredRegister<ImperfectRitual> IMPERFECT_RITUALS =
        DeferredRegister.create(RitualRegistry.IMPERFECT_RITUAL_REGISTRY_KEY, Constants.Mod.MODID);

    public static final DeferredHolder<ImperfectRitual, RitualSoulStainedBlood> SOUL_STAINED_BLOOD =
        IMPERFECT_RITUALS.register(Constants.Rituals.SOUL_STAINED_BLOOD, () -> new RitualSoulStainedBlood());

    public MalumCompat() {
        INSTANCE = this;
    }

    public static MalumCompat getInstance() {
        return INSTANCE;
    }

    public static void registerDeferred(IEventBus modEventBus) {
        IMPERFECT_RITUALS.register(modEventBus);
        Animus.LOGGER.debug("Registered Malum compatibility registries (imperfect rituals)");
    }

    @Override
    public void init() {
        Animus.LOGGER.debug("Initializing Malum compatibility");

        // Malum integration is passive - no event listeners needed
        // Integration happens through direct API calls from weapon items

        Animus.LOGGER.debug("Malum compatibility initialized successfully");
    }

    @Override
    public String getModId() {
        return "malum";
    }
}
