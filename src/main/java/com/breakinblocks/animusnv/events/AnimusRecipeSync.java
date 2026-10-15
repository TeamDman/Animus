package com.breakinblocks.animusnv.events;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusRecipeSerializers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public final class AnimusRecipeSync {

    private AnimusRecipeSync() {}

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(AnimusRecipeSerializers.SANGUINE_DYNAMO_TYPE.get());
    }
}
