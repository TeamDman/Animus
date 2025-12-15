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
        COMPAT_MODULES.put("ars_nouveau", () -> new ArsNouveauCompat());
        COMPAT_MODULES.put("malum", () -> new MalumCompat());
        // COMPAT_MODULES.put("botania", () -> new BotaniaCompat()); // Botania not available for 1.21.1 yet
    }

    /**
     * Register all DeferredRegisters for loaded compatibility modules
     * Must be called during mod construction, before registry events fire
     *
     * IMPORTANT: We use separate helper methods to avoid class verification issues.
     * If we reference IronsSpellsCompat directly in this method body, the JVM may
     * try to verify that class exists even when the if condition is false, causing
     * NoClassDefFoundError when Iron's Spells isn't installed.
     */
    public static void registerDeferredRegisters(IEventBus modEventBus) {
        // Register Iron's Spells compat DeferredRegisters if the mod is present
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registerIronsSpellsDeferred(modEventBus);
        }

        // Register Ars Nouveau compat DeferredRegisters if the mod is present
        if (ModList.get().isLoaded("ars_nouveau")) {
            registerArsNouveauDeferred(modEventBus);
        }

        // Register Malum compat DeferredRegisters if the mod is present
        if (ModList.get().isLoaded("malum")) {
            registerMalumDeferred(modEventBus);
        }
    }

    /**
     * Helper method that isolates the IronsSpellsCompat class reference.
     * This method should ONLY be called after verifying irons_spellbooks is loaded.
     *
     * We delegate to IronsSpellsCompatLoader to avoid referencing IronsSpellsCompat
     * directly in this class, which would cause class verification to fail when
     * Iron's Spells isn't installed.
     */
    private static void registerIronsSpellsDeferred(IEventBus modEventBus) {
        IronsSpellsCompatLoader.registerDeferred(modEventBus);
    }

    /**
     * Helper method that isolates the ArsNouveauCompat class reference.
     * This method should ONLY be called after verifying ars_nouveau is loaded.
     *
     * We delegate to ArsNouveauCompatLoader to avoid referencing ArsNouveauCompat
     * directly in this class, which would cause class verification to fail when
     * Ars Nouveau isn't installed.
     */
    private static void registerArsNouveauDeferred(IEventBus modEventBus) {
        ArsNouveauCompatLoader.registerDeferred(modEventBus);
    }

    /**
     * Helper method that isolates the MalumCompat class reference.
     * This method should ONLY be called after verifying malum is loaded.
     *
     * We delegate to MalumCompatLoader to avoid referencing MalumCompat
     * directly in this class, which would cause class verification to fail when
     * Malum isn't installed.
     */
    private static void registerMalumDeferred(IEventBus modEventBus) {
        MalumCompatLoader.registerDeferred(modEventBus);
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
