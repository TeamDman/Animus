package com.breakinblocks.animusnv.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.breakinblocks.animusnv.entities.EntityThrownSpear;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrownSpearRenderer extends EntityRenderer<EntityThrownSpear, ThrownSpearRenderer.SpearRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ThrownSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    public static class SpearRenderState extends EntityRenderState {
        public float xRot;
        public float yRot;
        public final ItemStackRenderState item = new ItemStackRenderState();
    }

    @Override
    public SpearRenderState createRenderState() {
        return new SpearRenderState();
    }

    @Override
    public void extractRenderState(EntityThrownSpear entity, SpearRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.yRot = entity.getYRot(partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        this.itemModelResolver.updateForNonLiving(state.item, getSpearStack(entity.getVariant()), ItemDisplayContext.GROUND, entity);
    }

    @Override
    public void submit(SpearRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot + 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot + 105.0F));

        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static ItemStack getSpearStack(String variant) {
        return switch (variant) {
            case "diamond" -> new ItemStack(AnimusItems.SPEAR_DIAMOND.get());
            case "bound" -> new ItemStack(AnimusItems.SPEAR_BOUND.get());
            case "sentient" -> new ItemStack(AnimusItems.SPEAR_SENTIENT.get());
            default -> new ItemStack(AnimusItems.SPEAR_IRON.get());
        };
    }
}
