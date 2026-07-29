package com.breakinblocks.animusnv;

import com.breakinblocks.animusnv.client.models.AnimusModelLayers;
import com.breakinblocks.animusnv.client.models.SpearModel;
import com.breakinblocks.animusnv.client.renderers.ThrownSpearRenderer;
import com.breakinblocks.animusnv.registry.AnimusEntityTypes;
import com.breakinblocks.animusnv.client.renderers.AnimusArrowRenderer;
import com.breakinblocks.animusnv.compat.EvilCraftCompat;
import com.breakinblocks.animusnv.compat.evilcraft.SanguineRectifierRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Constants.Mod.MODID, value = Dist.CLIENT)
public class AnimusClientSetup {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AnimusEntityTypes.THROWN_PILUM.get(), ThrownSpearRenderer::new);

        event.registerEntityRenderer(AnimusEntityTypes.SENTIENT_ARROW.get(), AnimusArrowRenderer::new);
        event.registerEntityRenderer(AnimusEntityTypes.HELLFORGED_ARROW.get(), AnimusArrowRenderer::new);

        if (ModList.get().isLoaded("evilcraft")) {
            registerEvilCraftRenderers(event);
        }
    }

    private static void registerEvilCraftRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
            EvilCraftCompat.SANGUINE_RECTIFIER_BE.get(),
            SanguineRectifierRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AnimusModelLayers.PILUM, SpearModel::createBodyLayer);
    }
}
