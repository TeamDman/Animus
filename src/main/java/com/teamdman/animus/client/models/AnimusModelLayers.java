package com.teamdman.animus.client.models;

import com.teamdman.animus.Constants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Defines custom model layers for Animus entities
 */
@OnlyIn(Dist.CLIENT)
public class AnimusModelLayers {
    public static final ModelLayerLocation PILUM = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "spear"),
        "main"
    );
}
