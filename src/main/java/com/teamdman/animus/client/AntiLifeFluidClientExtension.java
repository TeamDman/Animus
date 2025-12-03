package com.teamdman.animus.client;

import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;

/**
 * Client-only extension for AntiLife fluid rendering.
 */
public class AntiLifeFluidClientExtension implements IClientFluidTypeExtensions {
    public static final AntiLifeFluidClientExtension INSTANCE = new AntiLifeFluidClientExtension();

    @Override
    public ResourceLocation getStillTexture() {
        return Constants.Resource.fluidAntiLifeStill;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return Constants.Resource.fluidAntiLifeFlowing;
    }

    @Override
    public int getTintColor() {
        // White/light gray color for antilife
        return 0xFFEEEEEE;
    }
}
