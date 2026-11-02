package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import net.neoforged.bus.api.IEventBus;

public class IronsArtificeCompatLoader {

    public static void registerDeferred(IEventBus modEventBus) {
        IronsArtificeCompat.registerDeferred(modEventBus);
        Animus.LOGGER.debug("Registered Iron's Arms 'n Artifice compatibility deferred registries");
    }
}
