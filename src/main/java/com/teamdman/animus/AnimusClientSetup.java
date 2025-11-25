package com.teamdman.animus;

import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-side setup for Animus mod
 * Handles render layers and other client-only initialization
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AnimusClientSetup {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Set render layers for transparent blocks
            // Sapling uses cutout for transparency
            ItemBlockRenderTypes.setRenderLayer(AnimusBlocks.BLOCK_BLOOD_SAPLING.get(), RenderType.cutout());

            // Leaves use cutout_mipped for transparency (fancy graphics support)
            ItemBlockRenderTypes.setRenderLayer(AnimusBlocks.BLOCK_BLOOD_LEAVES.get(), RenderType.cutoutMipped());
        });
    }
}
