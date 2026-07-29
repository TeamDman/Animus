package com.breakinblocks.animusnv.client.renderers;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

public class AnimusArrowRenderer<T extends AbstractArrow> extends ArrowRenderer<T, ArrowRenderState> {
    private static final Identifier SPECTRAL_ARROW_LOCATION =
        Identifier.withDefaultNamespace("textures/entity/projectiles/spectral_arrow.png");

    public AnimusArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return SPECTRAL_ARROW_LOCATION;
    }
}
