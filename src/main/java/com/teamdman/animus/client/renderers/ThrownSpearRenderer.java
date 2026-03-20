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

        ItemStack spearStack = getSpearStack(entity.getVariant());

        // +90 instead of -90 to flip the model to face forward
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) + 90.0F));

        // +105 instead of +90 to tilt the tip slightly downward
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 105.0F));

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
        // Required by EntityRenderer but unused since we render the item model directly
        return SPEAR_IRON_TEXTURE;
    }
}
