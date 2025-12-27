package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Main compatibility handler for optional mod integrations
 * Uses lazy initialization to avoid ClassNotFoundException for missing mods
 */
public class CompatHandler {
    private static final Map<String, Supplier<ICompatModule>> COMPAT_MODULES = new HashMap<>();
    private static final Map<String, ICompatModule> LOADED_MODULES = new HashMap<>();

    static {
        // Register compatibility modules here
        // Using suppliers ensures classes are only loaded when the mod is present
        COMPAT_MODULES.put("irons_spellbooks", IronsSpellsCompat::new);
        COMPAT_MODULES.put("ars_nouveau", ArsNouveauCompat::new);
        COMPAT_MODULES.put("malum", MalumCompat::new);
        COMPAT_MODULES.put("botania", BotaniaCompat::new);
    }

    /**
     * Helper to safely check if Iron's Spellbooks integration is disabled via config
     * Returns false if config isn't loaded yet (allowing registration to proceed)
     */
    private static boolean isIronsSpellsDisabledByConfig() {
        try {
            return AnimusConfig.ironsSpells != null && !AnimusConfig.ironsSpells.enableIronsSpellsIntegration.get();
        } catch (IllegalStateException e) {
            // Config not loaded yet, allow registration to proceed
            return false;
        }
    }

    /**
     * Register all DeferredRegisters for loaded compatibility modules
     * Must be called during mod construction, before registry events fire
     */
    public static void registerDeferredRegisters(IEventBus modEventBus) {
        // Check for Irons Spells and register its items early
        // Also check config to allow users to disable integration for incompatible versions
        if (ModList.get().isLoaded("irons_spellbooks")) {
            if (isIronsSpellsDisabledByConfig()) {
                Animus.LOGGER.info("Iron's Spellbooks integration disabled via config");
            } else {
                try {
                    IronsSpellsCompat.registerDeferred(modEventBus);
                } catch (NoSuchMethodError e) {
                    Animus.LOGGER.error("Iron's Spellbooks API incompatibility detected. " +
                        "You may be using an incompatible pre-release version. " +
                        "Set ironsSpells.enableIntegration=false in animus-common.toml to disable this integration.", e);
                } catch (Exception e) {
                    Animus.LOGGER.error("Failed to register Irons Spells DeferredRegister", e);
                }
            }
        }

        // Check for Ars Nouveau and register its block entities early
        if (ModList.get().isLoaded("ars_nouveau")) {
            try {
                ArsNouveauCompat.registerDeferred(modEventBus);
            } catch (Exception e) {
                Animus.LOGGER.error("Failed to register Ars Nouveau DeferredRegister", e);
            }
        }

        // Check for Botania and register its block entities early
        if (ModList.get().isLoaded("botania")) {
            try {
                BotaniaCompat.registerDeferred(modEventBus);
            } catch (Exception e) {
                Animus.LOGGER.error("Failed to register Botania DeferredRegister", e);
            }
        }
    }

    /**
     * Initialize all available compatibility modules
     * Call this during common setup (after registries are populated)
     */
    public static void init() {
        COMPAT_MODULES.forEach((modId, supplier) -> {
            if (ModList.get().isLoaded(modId)) {
                // Check if Iron's Spellbooks integration is disabled via config
                if (modId.equals("irons_spellbooks") && isIronsSpellsDisabledByConfig()) {
                    Animus.LOGGER.info("Skipping Iron's Spellbooks module init (disabled via config)");
                    return;
                }

                try {
                    ICompatModule module = supplier.get();
                    module.init();
                    LOADED_MODULES.put(modId, module);
                    Animus.LOGGER.info("Loaded compatibility module for: {}", modId);
                } catch (NoSuchMethodError e) {
                    Animus.LOGGER.error("API incompatibility detected for module: {}. " +
                        "You may be using an incompatible mod version.", modId, e);
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
     * Returns false if disabled via config or if module failed to load
     */
    public static boolean isIronsSpellsLoaded() {
        if (isIronsSpellsDisabledByConfig()) {
            return false;
        }
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
