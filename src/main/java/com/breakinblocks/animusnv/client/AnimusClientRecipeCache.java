package com.breakinblocks.animusnv.client;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

@EventBusSubscriber(modid = Constants.Mod.MODID, value = Dist.CLIENT)
public final class AnimusClientRecipeCache {
    private static RecipeMap recipes = RecipeMap.EMPTY;

    private AnimusClientRecipeCache() {}

    public static RecipeMap get() {
        return recipes;
    }

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        recipes = event.getRecipeMap();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        recipes = RecipeMap.EMPTY;
    }
}
