package com.breakinblocks.animusnv.client.renderers;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.blockentities.BlockEntitySanguineDynamo;
import com.breakinblocks.animusnv.blocks.BlockSanguineDynamo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public class SanguineDynamoRenderer extends GeoBlockRenderer<BlockEntitySanguineDynamo> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "sanguine_dynamo");
    private static final String ORB_SOCKET = "orb_socket";

    public SanguineDynamoRenderer() {
        super(new DefaultedBlockGeoModel<>(ID));
        addRenderLayer(new AutoGlowingGeoLayer<>(this) {
            @Override
            public void render(PoseStack poseStack, BlockEntitySanguineDynamo animatable, BakedGeoModel bakedModel,
                               RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                               float partialTick, int packedLight, int packedOverlay) {
                if (isActive(animatable)) {
                    super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
                }
            }
        });
        addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Override
            protected ItemStack getStackForBone(GeoBone bone, BlockEntitySanguineDynamo animatable) {
                return ORB_SOCKET.equals(bone.getName()) ? animatable.getOrbStack() : null;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                              BlockEntitySanguineDynamo animatable, MultiBufferSource bufferSource,
                                              float partialTick, int packedLight, int packedOverlay) {
                if (stack.isEmpty()) return;
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.scale(0.55f, 0.55f, 0.55f);
                int light = isActive(animatable) ? LightTexture.FULL_BRIGHT : packedLight;
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource, animatable.getLevel(), 0);
            }
        });
    }

    private static boolean isActive(BlockEntitySanguineDynamo animatable) {
        BlockState state = animatable.getBlockState();
        return state.hasProperty(BlockSanguineDynamo.ACTIVE) && state.getValue(BlockSanguineDynamo.ACTIVE);
    }
}
