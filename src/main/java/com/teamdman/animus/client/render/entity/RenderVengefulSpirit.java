package com.teamdman.animus.client.render.entity;

import com.teamdman.animus.entity.EntityGenericDemon;
import com.teamdman.animus.entity.EntityVengefulSpirit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;


public class RenderVengefulSpirit extends RenderBiped<EntityVengefulSpirit> {


	public RenderVengefulSpirit(RenderManager renderManager) {
		super(renderManager, new ModelPlayer(0.0F, false), 0.0F);
	}

	@Override
	public void doRender(@Nonnull EntityVengefulSpirit entity, double x, double y, double z, float entityYaw, float partialTicks) {

		this.addLayer(new EntityGenericDemon<EntityVengefulSpirit>(this, new ModelPlayer(0.0F, false)));
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
		GlStateManager.scale(0.5, 0.5, 0.5);
		super.doRender(entity, 0, 0, 0, entityYaw, partialTicks);
		GlStateManager.popMatrix();
	}

	@Nonnull
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntityVengefulSpirit entity) {
		Minecraft mc = Minecraft.getMinecraft();

		if (!(mc.getRenderViewEntity() instanceof AbstractClientPlayer))
			return DefaultPlayerSkin.getDefaultSkinLegacy();

		return ((AbstractClientPlayer) mc.getRenderViewEntity()).getLocationSkin();
	}

}
