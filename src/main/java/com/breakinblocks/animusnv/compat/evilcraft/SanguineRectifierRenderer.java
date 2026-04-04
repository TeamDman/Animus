package com.breakinblocks.animusnv.compat.evilcraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SanguineRectifierRenderer implements BlockEntityRenderer<BlockEntitySanguineRectifier> {

    public SanguineRectifierRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntitySanguineRectifier be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack orbStack = be.getOrbStack();
        if (orbStack.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        poseStack.pushPose();

        poseStack.translate(0.5, 1.5, 0.5);

        float rotation = (float) (720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        poseStack.scale(0.5F, 0.5F, 0.5F);

        BakedModel bakedModel = itemRenderer.getModel(orbStack, be.getLevel(), null, 1);
        itemRenderer.render(orbStack, ItemDisplayContext.FIXED, true, poseStack,
            bufferSource, packedLight, packedOverlay, bakedModel);

        poseStack.popPose();
    }
}
