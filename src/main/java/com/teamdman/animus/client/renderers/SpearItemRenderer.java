package com.teamdman.animus.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teamdman.animus.Constants;
import com.teamdman.animus.items.ItemSpear;
import com.teamdman.animus.items.ItemSpearBound;
import com.teamdman.animus.items.ItemSpearSentient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class SpearItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final SpearItemRenderer INSTANCE = new SpearItemRenderer();

    public SpearItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
              Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        
        if (displayContext == ItemDisplayContext.GUI) {
            // For GUI, item frame, and ground display, render the 2D sprite
            renderGuiSprite(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else {
            // For all other contexts (held in hand), render the 3D model
            renderModel(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private void renderGuiSprite(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                                 MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        ResourceLocation textureLocation = getGuiTexture(stack);
        
        poseStack.pushPose();
        
        // Render using entity cutout with the texture file directly
        RenderType renderType = RenderType.entityCutout(textureLocation);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        
        Matrix4f matrix = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        
        // Render a flat quad (16x16 texture mapped to 1x1 quad)
        // UV coordinates are 0-1 for full texture
        vertexConsumer.vertex(matrix, 0, 0, 0.5f).color(255, 255, 255, 255).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 1, 0, 0.5f).color(255, 255, 255, 255).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 1, 1, 0.5f).color(255, 255, 255, 255).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0, 0, 1).endVertex();
        vertexConsumer.vertex(matrix, 0, 1, 0.5f).color(255, 255, 255, 255).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, 0, 0, 1).endVertex();
        
        poseStack.popPose();
    }

    private void renderModel(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        // Get the baked model directly from the model manager
        ModelResourceLocation modelLocation = getModelLocation(stack);
        BakedModel model = mc.getModelManager().getModel(modelLocation);

        // Check if player is charging the spear throw
        boolean isCharging = false;
        if (mc.player != null && mc.player.isUsingItem()) {
            ItemStack usingItem = mc.player.getUseItem();
            if (usingItem.getItem() instanceof ItemSpear) {
                isCharging = true;
            }
        }

        poseStack.pushPose();

        // Apply display transforms
        model = model.applyTransform(displayContext, poseStack, false);
        poseStack.translate(-0.5, -0.5, -0.5);

        // DEBUG: Apply scale to test - should make spear 2x larger
        if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                        || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                        || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                        || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            poseStack.scale(2.0f, 2.0f, 2.0f);
        }

        // Render using the standard item rendering path which properly uses the poseStack
        RenderType renderType = Sheets.cutoutBlockSheet();
        VertexConsumer vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, renderType, true, stack.hasFoil());

        // Render each quad from the model manually using the poseStack transform
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
            for (net.minecraft.client.renderer.block.model.BakedQuad quad : model.getQuads(null, direction, mc.level.random)) {
                vertexConsumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
            }
        }
        // Also render non-directional quads
        for (net.minecraft.client.renderer.block.model.BakedQuad quad : model.getQuads(null, null, mc.level.random)) {
            vertexConsumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private ModelResourceLocation getModelLocation(ItemStack stack) {
        // Load the _3d model variants which have the actual geometry (not builtin/entity)
        if (stack.getItem() instanceof ItemSpearSentient) {
            return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "spear_sentient_3d"), "inventory");
        } else if (stack.getItem() instanceof ItemSpearBound boundSpear) {
            if (boundSpear.isActivated(stack)) {
                return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "spear_bound_activated_3d"), "inventory");
            }
            return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "spear_bound_3d"), "inventory");
        } else if (stack.getItem() instanceof ItemSpear spear) {
            if (spear.getTier() == Tiers.DIAMOND) {
                return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "spear_diamond_3d"), "inventory");
            }
            return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "spear_iron_3d"), "inventory");
        }
        return new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "spear_iron_3d"), "inventory");
    }

    private ResourceLocation getGuiTexture(ItemStack stack) {
        if (stack.getItem() instanceof ItemSpearSentient) {
            return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/item/sentient_spear_item.png");
        } else if (stack.getItem() instanceof ItemSpearBound boundSpear) {
            if (boundSpear.isActivated(stack)) {
                return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/item/bound_spear_activated_item.png");
            }
            return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/item/bound_spear_item.png");
        } else if (stack.getItem() instanceof ItemSpear spear) {
            if (spear.getTier() == Tiers.DIAMOND) {
                return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/item/diamond_spear_item.png");
            }
            return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/item/iron_spear_item.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "textures/item/iron_spear_item.png");
    }
}
