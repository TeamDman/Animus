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
        COMPAT_MODULES.put("evilcraft", () -> new EvilCraftCompat());
        COMPAT_MODULES.put("dynamictrees", () -> new DynamicTreesCompat());
        COMPAT_MODULES.put("irons_artifice", () -> new IronsArtificeCompat());
    }

    /**
     * Delegates to separate loader classes to avoid class verification issues
     * (JVM would fail verifying compat class references even inside false branches).
     */
    public static void registerDeferredRegisters(IEventBus modEventBus) {
        if (ModList.get().isLoaded("evilcraft")) {
            registerEvilCraftDeferred(modEventBus);
        }

        if (ModList.get().isLoaded("dynamictrees")) {
            registerDynamicTreesDeferred(modEventBus);
        }

        if (ModList.get().isLoaded("irons_artifice")) {
            registerIronsArtificeDeferred(modEventBus);
        }
    }

    private static void registerEvilCraftDeferred(IEventBus modEventBus) {
        EvilCraftCompatLoader.registerDeferred(modEventBus);
    }

    private static void registerDynamicTreesDeferred(IEventBus modEventBus) {
        DynamicTreesCompatLoader.registerDeferred(modEventBus);
    }

    private static void registerIronsArtificeDeferred(IEventBus modEventBus) {
        IronsArtificeCompatLoader.registerDeferred(modEventBus);
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

    public static boolean isDynamicTreesLoaded() {
        return isModuleLoaded("dynamictrees");
    }

    public static boolean isIronsArtificeLoaded() {
        return isModuleLoaded("irons_artifice");
    }

}
