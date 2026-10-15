package com.breakinblocks.animusnv.client.renderers;

import com.breakinblocks.animusnv.items.ItemSanguineDynamo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SanguineDynamoItemRenderer extends GeoItemRenderer<ItemSanguineDynamo> {

    public SanguineDynamoItemRenderer() {
        super(new DefaultedBlockGeoModel<>(SanguineDynamoRenderer.ID));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preRender(PoseStack poseStack, ItemSanguineDynamo animatable, BakedGeoModel model,
                          MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                          float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender) {
            poseStack.translate(0, -0.51f, 0);
        }
    }
}
