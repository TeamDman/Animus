package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import net.neoforged.bus.api.IEventBus;

public class EvilCraftCompatLoader {

    public static void registerDeferred(IEventBus modEventBus) {
        EvilCraftCompat.registerDeferred(modEventBus);
        Animus.LOGGER.debug("Registered EvilCraft compatibility deferred registries");
    }
}
