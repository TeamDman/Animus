package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Main compatibility handler for optional mod integrations
 * Uses lazy initialization to avoid ClassNotFoundException for missing mods
 *
 * NOTE: Most compat modules are disabled until the dependent mods have 1.21.1 versions
 */
public class CompatHandler {
    private static final Map<String, Supplier<ICompatModule>> COMPAT_MODULES = new HashMap<>();
    private static final Map<String, ICompatModule> LOADED_MODULES = new HashMap<>();

    static {
        // Register compatibility modules here
        // IMPORTANT: Use lambdas with Class.forName or full class instantiation inside the lambda
        // to avoid eager class loading. Method references like IronsSpellsCompat::new will
        // cause the class to be loaded immediately, which fails if the dependency is missing.
        COMPAT_MODULES.put("irons_spellbooks", () -> new IronsSpellsCompat());
        // COMPAT_MODULES.put("ars_nouveau", () -> new ArsNouveauCompat());
        COMPAT_MODULES.put("malum", () -> new MalumCompat());
        // COMPAT_MODULES.put("botania", () -> new BotaniaCompat()); // Botania not available for 1.21.1 yet
    }

    /**
     * Register all DeferredRegisters for loaded compatibility modules
     * Must be called during mod construction, before registry events fire
     */
    public static void registerDeferredRegisters(IEventBus modEventBus) {
        // Register Iron's Spells compat DeferredRegisters if the mod is present
        if (ModList.get().isLoaded("irons_spellbooks")) {
            IronsSpellsCompat.registerDeferred(modEventBus);
            Animus.LOGGER.info("Registered Iron's Spells compatibility deferred registries");
        }
    }

    /**
     * Initialize all available compatibility modules
     * Call this during common setup (after registries are populated)
     */
    public static void init() {
        COMPAT_MODULES.forEach((modId, supplier) -> {
            if (ModList.get().isLoaded(modId)) {
                try {
                    ICompatModule module = supplier.get();
                    module.init();
                    LOADED_MODULES.put(modId, module);
                    Animus.LOGGER.info("Loaded compatibility module for: {}", modId);
                } catch (Exception e) {
                    Animus.LOGGER.error("Failed to load compatibility module for: {}", modId, e);
                }
            }
        });
    }

    /**
     * Check if a specific compatibility module is loaded
     */
    public static boolean isModuleLoaded(String modId) {
        return LOADED_MODULES.containsKey(modId);
    }

    /**
     * Get a loaded compatibility module
     */
    public static ICompatModule getModule(String modId) {
        return LOADED_MODULES.get(modId);
    }

    /**
     * Check if Irons Spells integration is active
     */
    public static boolean isIronsSpellsLoaded() {
        return isModuleLoaded("irons_spellbooks");
    }

    /**
     * Check if Ars Nouveau integration is active
     */
    public static boolean isArsNouveauLoaded() {
        return isModuleLoaded("ars_nouveau");
    }

    /**
     * Check if Malum integration is active
     */
    public static boolean isMalumLoaded() {
        return isModuleLoaded("malum");
    }

    /**
     * Check if Botania integration is active
     */
    public static boolean isBotaniaLoaded() {
        return isModuleLoaded("botania");
    }
}
