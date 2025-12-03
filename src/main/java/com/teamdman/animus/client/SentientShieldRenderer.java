package com.teamdman.animus.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Client-only renderer for the Sentient Shield.
 * This class is separated to prevent class loading issues on dedicated servers.
 */
public class SentientShieldRenderer extends BlockEntityWithoutLevelRenderer {
    public static final SentientShieldRenderer INSTANCE = new SentientShieldRenderer();

    private SentientShieldRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
              Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                            MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        // Scale down the shield by 20% when rendering in GUI (hotbar/inventory)
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(0.8f, 0.8f, 0.8f);
        }
        // Call the default shield renderer
        Minecraft.getInstance().getItemRenderer()
            .getBlockEntityRenderer()
            .renderByItem(stack, displayContext, poseStack, buffer, combinedLight, combinedOverlay);
    }
}
