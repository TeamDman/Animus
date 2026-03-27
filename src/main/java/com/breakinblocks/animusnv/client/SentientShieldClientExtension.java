package com.breakinblocks.animusnv.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class SentientShieldClientExtension implements IClientItemExtensions {
    public static final SentientShieldClientExtension INSTANCE = new SentientShieldClientExtension();

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return SentientShieldRenderer.INSTANCE;
    }
}
