package com.teamdman.animus.client;

import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class LivingTerraFluidClientExtension implements IClientFluidTypeExtensions {
    public static final LivingTerraFluidClientExtension INSTANCE = new LivingTerraFluidClientExtension();

    @Override
    public ResourceLocation getStillTexture() {
        return Constants.Resource.fluidLivingTerraStill;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return Constants.Resource.fluidLivingTerraFlowing;
    }

    @Override
    public int getTintColor() {
        return 0xFF8B6F47;
    }
}
