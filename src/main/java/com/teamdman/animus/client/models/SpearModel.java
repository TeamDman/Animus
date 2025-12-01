package com.teamdman.animus.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Spear Model - Based on vanilla trident but customizable
 * Edit this model in Blockbench and export to replace the createBodyLayer method
 */
@OnlyIn(Dist.CLIENT)
public class SpearModel extends Model {
    private final ModelPart root;

    public SpearModel(ModelPart root) {
        super(RenderType::entitySolid);
        this.root = root;
    }

    /**
     * Creates the default spear model layer definition
     * This is based on the vanilla trident model
     * Replace this method with Blockbench export when you have your custom model
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Pole (main shaft) - 56 units long, 1x1 thickness
        partdefinition.addOrReplaceChild("pole",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-0.5F, -0.5F, -31.0F, 1.0F, 1.0F, 56.0F),
            PartPose.ZERO);

        // Tip right blade
        partdefinition.addOrReplaceChild("tip_right",
            CubeListBuilder.create()
                .texOffs(0, 2)
                .addBox(-0.5F, -0.5F, -35.0F, 1.0F, 1.0F, 4.0F),
            PartPose.ZERO);

        // Base left (back of trident)
        partdefinition.addOrReplaceChild("base_left",
            CubeListBuilder.create()
                .texOffs(4, 0)
                .addBox(0.0F, -1.5F, 24.0F, 1.0F, 3.0F, 1.0F),
            PartPose.ZERO);

        // Base right (back of trident)
        partdefinition.addOrReplaceChild("base_right",
            CubeListBuilder.create()
                .texOffs(4, 3)
                .addBox(-1.0F, -1.5F, 24.0F, 1.0F, 3.0F, 1.0F),
            PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
