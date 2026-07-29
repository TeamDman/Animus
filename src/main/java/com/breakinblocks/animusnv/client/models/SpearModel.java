package com.breakinblocks.animusnv.client.models;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class SpearModel extends Model.Simple {

    public SpearModel(ModelPart root) {
        super(root, RenderTypes::entitySolid);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("pole",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-0.5F, -0.5F, -31.0F, 1.0F, 1.0F, 56.0F),
            PartPose.ZERO);

        partdefinition.addOrReplaceChild("tip_right",
            CubeListBuilder.create()
                .texOffs(0, 2)
                .addBox(-0.5F, -0.5F, -35.0F, 1.0F, 1.0F, 4.0F),
            PartPose.ZERO);

        partdefinition.addOrReplaceChild("base_left",
            CubeListBuilder.create()
                .texOffs(4, 0)
                .addBox(0.0F, -1.5F, 24.0F, 1.0F, 3.0F, 1.0F),
            PartPose.ZERO);

        partdefinition.addOrReplaceChild("base_right",
            CubeListBuilder.create()
                .texOffs(4, 3)
                .addBox(-1.0F, -1.5F, 24.0F, 1.0F, 3.0F, 1.0F),
            PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}
