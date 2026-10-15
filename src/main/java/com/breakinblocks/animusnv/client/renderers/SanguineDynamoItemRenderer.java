package com.breakinblocks.animusnv.client.renderers;

import com.breakinblocks.animusnv.items.ItemSanguineDynamo;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;

public class SanguineDynamoItemRenderer extends GeoItemRenderer<ItemSanguineDynamo> {

    public SanguineDynamoItemRenderer() {
        super(new DefaultedBlockGeoModel<ItemSanguineDynamo>(SanguineDynamoRenderer.ID));
        withRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> info) {
        info.poseStack().translate(0.5f, 0f, 0.5f);
    }
}
