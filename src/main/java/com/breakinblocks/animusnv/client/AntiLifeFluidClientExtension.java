package com.breakinblocks.animusnv.client;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class AntiLifeFluidClientExtension implements IClientFluidTypeExtensions {
    public static final AntiLifeFluidClientExtension INSTANCE = new AntiLifeFluidClientExtension();

    public Identifier getStillTexture() {
        return Constants.Resource.fluidAntiLifeStill;
    }

    public Identifier getFlowingTexture() {
        return Constants.Resource.fluidAntiLifeFlowing;
    }

    public int getTintColor() {
        return 0xFFEEEEEE;
    }
}
