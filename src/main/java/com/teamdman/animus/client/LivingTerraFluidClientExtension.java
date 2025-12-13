package com.teamdman.animus.client;

import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Client-only extension for Living Terra fluid rendering.
 */
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
        // Brown color for living terra
        return 0xFF8B6F47;
    }
}
