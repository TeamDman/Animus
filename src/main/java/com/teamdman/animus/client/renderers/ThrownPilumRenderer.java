package com.teamdman.animus.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.teamdman.animus.Constants;
import com.teamdman.animus.client.models.AnimusModelLayers;
import com.teamdman.animus.client.models.PilumModel;
import com.teamdman.animus.entities.EntityThrownPilum;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Renderer for thrown pilum entities
 * Renders the pilum as a 3D model similar to tridents but with custom texture
 */
@OnlyIn(Dist.CLIENT)
public class ThrownPilumRenderer extends EntityRenderer<EntityThrownPilum> {
    private static final ResourceLocation PILUM_IRON_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/entity/pilum_iron.png");
    private static final ResourceLocation PILUM_DIAMOND_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/entity/pilum_diamond.png");
    private static final ResourceLocation PILUM_BOUND_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/entity/pilum_bound.png");

    private final PilumModel model;

    public ThrownPilumRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PilumModel(context.bakeLayer(AnimusModelLayers.PILUM));
    }

    @Override
    public void render(EntityThrownPilum entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Rotate to match entity yaw (horizontal direction) and flip 180 degrees
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 90.0F));

        // Rotate to match entity pitch (vertical angle) with 45 degree offset to lean tip back
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 45.0F));

        // Additional rotation to align model tip forward (model is built along -Z axis)
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, this.model.renderType(this.getTextureLocation(entity)), false, entity.isFoil());

        // Render the pilum model
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityThrownPilum entity) {
        String variant = entity.getVariant();
        return switch (variant) {
            case "diamond" -> PILUM_DIAMOND_TEXTURE;
            case "bound" -> PILUM_BOUND_TEXTURE;
            default -> PILUM_IRON_TEXTURE;
        };
    }

}
