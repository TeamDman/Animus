package com.teamdman.animus.client;

import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

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
        return 0xFFEEEEEE;
    }
}
