package com.teamdman.animus.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntityThrownSpear;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Renderer for thrown spear entities
 * Renders the spear using the item's 3D model
 */
@OnlyIn(Dist.CLIENT)
public class ThrownSpearRenderer extends EntityRenderer<EntityThrownSpear> {
    private static final ResourceLocation SPEAR_IRON_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/entity/spear_iron.png");

    public ThrownSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityThrownSpear entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Get the correct item stack based on variant
        ItemStack spearStack = getSpearStack(entity.getVariant());

        // Rotate to match entity yaw (horizontal direction) - adding 90 instead of subtracting to flip it
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 90.0F));

        // Rotate to match entity pitch (vertical angle) - adding extra 15 degrees to point tip down
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 105.0F));

        // Render the spear item model
        Minecraft.getInstance().getItemRenderer().renderStatic(
            spearStack,
            ItemDisplayContext.GROUND,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            entity.level(),
            entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Get the appropriate spear ItemStack based on variant
     */
    private ItemStack getSpearStack(String variant) {
        return switch (variant) {
            case "diamond" -> new ItemStack(AnimusItems.SPEAR_DIAMOND.get());
            case "bound" -> new ItemStack(AnimusItems.SPEAR_BOUND.get());
            case "sentient" -> new ItemStack(AnimusItems.SPEAR_SENTIENT.get());
            default -> new ItemStack(AnimusItems.SPEAR_IRON.get());
        };
    }

    @Override
    public ResourceLocation getTextureLocation(EntityThrownSpear entity) {
        // This is still needed for the entity renderer system but won't be used since we render the item
        return SPEAR_IRON_TEXTURE;
    }
}
