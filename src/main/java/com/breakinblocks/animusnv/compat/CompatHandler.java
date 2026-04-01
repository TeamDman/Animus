package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
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
        // Use lambdas (not method references) to avoid eager class loading when dependency is missing
        COMPAT_MODULES.put("irons_spellbooks", () -> new IronsSpellsCompat());
        COMPAT_MODULES.put("ars_nouveau", () -> new ArsNouveauCompat());
        COMPAT_MODULES.put("malum", () -> new MalumCompat());
        COMPAT_MODULES.put("evilcraft", () -> new EvilCraftCompat());
        // Botania not available for 1.21.1 yet
        // COMPAT_MODULES.put("botania", () -> new BotaniaCompat());
    }

    /**
     * Delegates to separate loader classes to avoid class verification issues
     * (JVM would fail verifying compat class references even inside false branches).
     */
    public static void registerDeferredRegisters(IEventBus modEventBus) {
        if (ModList.get().isLoaded("irons_spellbooks")) {
            registerIronsSpellsDeferred(modEventBus);
        }

        if (ModList.get().isLoaded("ars_nouveau")) {
            registerArsNouveauDeferred(modEventBus);
        }

        if (ModList.get().isLoaded("malum")) {
            registerMalumDeferred(modEventBus);
        }
    }

    private static void registerIronsSpellsDeferred(IEventBus modEventBus) {
        IronsSpellsCompatLoader.registerDeferred(modEventBus);
    }

    private static void registerArsNouveauDeferred(IEventBus modEventBus) {
        ArsNouveauCompatLoader.registerDeferred(modEventBus);
    }

    private static void registerMalumDeferred(IEventBus modEventBus) {
        MalumCompatLoader.registerDeferred(modEventBus);
    }

    public static void init() {
        COMPAT_MODULES.forEach((modId, supplier) -> {
            if (ModList.get().isLoaded(modId)) {
                try {
                    ICompatModule module = supplier.get();
                    module.init();
                    LOADED_MODULES.put(modId, module);
                    Animus.LOGGER.debug("Loaded compatibility module for: {}", modId);
                } catch (Exception e) {
                    Animus.LOGGER.error("Failed to load compatibility module for: {}", modId, e);
                }
            }
        });
    }

    public static boolean isModuleLoaded(String modId) {
        return LOADED_MODULES.containsKey(modId);
    }

    public static ICompatModule getModule(String modId) {
        return LOADED_MODULES.get(modId);
    }

    public static boolean isIronsSpellsLoaded() {
        return isModuleLoaded("irons_spellbooks");
    }

    public static boolean isArsNouveauLoaded() {
        return isModuleLoaded("ars_nouveau");
    }

    public static boolean isMalumLoaded() {
        return isModuleLoaded("malum");
    }

    public static boolean isBotaniaLoaded() {
        return isModuleLoaded("botania");
    }

    public static boolean isEvilCraftLoaded() {
        return isModuleLoaded("evilcraft");
    }
}
