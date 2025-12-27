package com.teamdman.animus.client.renderers;

import com.teamdman.animus.Constants;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;

/**
 * Simple arrow renderer for Animus custom arrows
 * Uses the spectral arrow texture for a glowing effect
 */
public class AnimusArrowRenderer<T extends AbstractArrow> extends ArrowRenderer<T> {
    private static final ResourceLocation SPECTRAL_ARROW_LOCATION =
        ResourceLocation.withDefaultNamespace("textures/entity/projectiles/spectral_arrow.png");

    public AnimusArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return SPECTRAL_ARROW_LOCATION;
    }
}
