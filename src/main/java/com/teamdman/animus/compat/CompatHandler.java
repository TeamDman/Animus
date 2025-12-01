package com.teamdman.animus.compat;

import com.teamdman.animus.Animus;
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
    }

    /**
     * Initialize all available compatibility modules
     * Call this during mod construction or common setup
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
}
