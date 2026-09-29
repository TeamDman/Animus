package com.breakinblocks.animusnv.compat;

import com.breakinblocks.animusnv.Animus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;

public class DynamicTreesCompatLoader {

    public static void registerDeferred(IEventBus modEventBus) {
        DynamicTreesCompat.registerDeferred(modEventBus);
        Animus.LOGGER.debug("Registered Dynamic Trees compatibility registries");
    }

    public static boolean plantBloodTree(ServerLevel level, BlockPos pos) {
        return DynamicTreesCompat.plantBloodTree(level, pos);
    }
}
