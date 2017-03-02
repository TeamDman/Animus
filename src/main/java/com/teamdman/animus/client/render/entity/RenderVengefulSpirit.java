package com.teamdman.animus.client.render.entity;

import javax.annotation.Nonnull;

import com.teamdman.animus.entity.EntityVengefulSpirit;

import WayofTime.bloodmagic.client.helper.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

public class RenderVengefulSpirit extends RenderBiped<EntityVengefulSpirit>{


	public RenderVengefulSpirit(RenderManager renderManager) {
		super(renderManager, new ModelPlayer(0.0F, false), 0F);
	}
	
	@Override
	public void doRender(@Nonnull EntityVengefulSpirit spirit, double par2, double par4, double par6, float par8, float par9) {

		ShaderHelper.useShader(ShaderHelper.psiBar, 20);
		super.doRender(spirit, par2, par4, par6, par8, par9);
		ShaderHelper.releaseShader();
	}

	@Nonnull
	@Override
	protected ResourceLocation getEntityTexture(@Nonnull EntityVengefulSpirit entity) {
		Minecraft mc = Minecraft.getMinecraft();

		if(!(mc.getRenderViewEntity() instanceof AbstractClientPlayer))
			return DefaultPlayerSkin.getDefaultSkinLegacy();

		return ((AbstractClientPlayer) mc.getRenderViewEntity()).getLocationSkin();
	}
	
}
