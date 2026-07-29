package com.breakinblocks.animusnv.client;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class LivingTerraFluidClientExtension implements IClientFluidTypeExtensions {
    public static final LivingTerraFluidClientExtension INSTANCE = new LivingTerraFluidClientExtension();

    public Identifier getStillTexture() {
        return Constants.Resource.fluidLivingTerraStill;
    }

    public Identifier getFlowingTexture() {
        return Constants.Resource.fluidLivingTerraFlowing;
    }

    public int getTintColor() {
        return 0xFF8B6F47;
    }
}
