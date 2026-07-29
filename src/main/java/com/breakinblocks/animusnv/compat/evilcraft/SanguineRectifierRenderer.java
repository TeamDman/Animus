package com.breakinblocks.animusnv.compat.evilcraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SanguineRectifierRenderer implements BlockEntityRenderer<BlockEntitySanguineRectifier, SanguineRectifierRenderer.State> {

    private final ItemModelResolver itemModelResolver;

    public SanguineRectifierRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public boolean present;
        public float itemRotation;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BlockEntitySanguineRectifier be, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumbling);
        ItemStack orbStack = be.getOrbStack();
        state.present = !orbStack.isEmpty();
        if (state.present) {
            this.itemModelResolver.updateForTopItem(state.item, orbStack, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
        } else {
            state.item.clear();
        }
        state.itemRotation = 720.0F * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.present) return;
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.itemRotation));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
