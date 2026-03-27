package com.breakinblocks.animusnv.client.models;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimusModelLayers {
    public static final ModelLayerLocation PILUM = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "spear"),
        "main"
    );
}
