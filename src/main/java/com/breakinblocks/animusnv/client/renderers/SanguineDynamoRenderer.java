package com.breakinblocks.animusnv.client.renderers;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.blockentities.BlockEntitySanguineDynamo;
import com.breakinblocks.animusnv.blocks.BlockSanguineDynamo;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class SanguineDynamoRenderer extends GeoBlockRenderer<BlockEntitySanguineDynamo, SanguineDynamoRenderer.State> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "sanguine_dynamo");
    private static final String ORB_SOCKET = "orb_socket";

    private final ItemModelResolver itemModelResolver;

    public SanguineDynamoRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<BlockEntitySanguineDynamo>(ID));
        this.itemModelResolver = context.itemModelResolver();
        withRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            public void submitRenderTask(RenderPassInfo<State> info, SubmitNodeCollector collector) {
                if (info.renderState().active) {
                    super.submitRenderTask(info, collector);
                }
            }
        });
        withRenderLayer(new OrbLayer(this));
    }

    public static class State extends BlockEntityRenderState {
        public final ItemStackRenderState orb = new ItemStackRenderState();
        public boolean active;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(BlockEntitySanguineDynamo dynamo, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        super.extractRenderState(dynamo, state, partialTick, cameraPos, crumbling);
        BlockState blockState = dynamo.getBlockState();
        state.active = blockState.hasProperty(BlockSanguineDynamo.ACTIVE) && blockState.getValue(BlockSanguineDynamo.ACTIVE);
        ItemStack orb = dynamo.getOrbStack();
        if (orb.isEmpty()) {
            state.orb.clear();
        } else {
            itemModelResolver.updateForTopItem(state.orb, orb, ItemDisplayContext.FIXED, dynamo.getLevel(), null, 0);
        }
    }

    private static class OrbLayer extends GeoRenderLayer<BlockEntitySanguineDynamo, Void, State> {

        OrbLayer(SanguineDynamoRenderer renderer) {
            super(renderer);
        }

        @Override
        public void addPerBoneRender(RenderPassInfo<State> info, BiConsumer<GeoBone, PerBoneRender<State>> consumer) {
            State state = info.renderState();
            if (state.orb.isEmpty()) return;
            info.model().getBone(ORB_SOCKET).ifPresent(bone -> consumer.accept(bone, (pass, socket, collector) -> {
                PoseStack poseStack = pass.poseStack();
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.scale(0.55f, 0.55f, 0.55f);
                int light = state.active ? LightCoordsUtil.FULL_BRIGHT : pass.packedLight();
                state.orb.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }));
        }
    }
}
