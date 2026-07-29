package com.breakinblocks.animusnv.client.models;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class AnimusModelLayers {
    public static final ModelLayerLocation PILUM = new ModelLayerLocation(
        Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "spear"),
        "main"
    );
}
