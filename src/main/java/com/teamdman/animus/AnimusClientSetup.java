package com.teamdman.animus;

import com.teamdman.animus.items.sigils.ItemSigilToggleableBase;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
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

            // Register item properties for toggleable sigils
            registerToggleableSigilProperty(AnimusItems.SIGIL_BUILDER.get());
            registerToggleableSigilProperty(AnimusItems.SIGIL_LEECH.get());
            registerToggleableSigilProperty(AnimusItems.SIGIL_TRANSPOSITION.get());
        });
    }

    /**
     * Registers the "activated" item property for toggleable sigils
     * This allows models to switch textures based on activation state
     */
    private static void registerToggleableSigilProperty(net.minecraft.world.item.Item item) {
        ItemProperties.register(item,
            ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "activated"),
            (stack, level, entity, seed) -> {
                if (item instanceof ItemSigilToggleableBase toggleable) {
                    return toggleable.getActivated(stack) ? 1.0F : 0.0F;
                }
                return 0.0F;
            }
        );
    }
}
