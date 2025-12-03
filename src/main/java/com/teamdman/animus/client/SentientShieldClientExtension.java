package com.teamdman.animus.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Client-only extension for Sentient Shield rendering.
 */
public class SentientShieldClientExtension implements IClientItemExtensions {
    public static final SentientShieldClientExtension INSTANCE = new SentientShieldClientExtension();

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return SentientShieldRenderer.INSTANCE;
    }
}
