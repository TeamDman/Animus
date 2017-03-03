package com.teamdman.animus.client.render.entity;

import javax.annotation.Nonnull;

import com.teamdman.animus.entity.EntityGenericDemon;
import com.teamdman.animus.entity.EntityVengefulSpirit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;


public class RenderVengefulSpirit extends RenderPlayer{


	public RenderVengefulSpirit(RenderManager renderManager) {
		super(renderManager);
	}
	
/*	@Override
	public void doRender(@Nonnull EntityVengefulSpirit spirit, double par2, double par4, double par6, float par8, float par9) {
		
		//this.addLayer(new EntityGenericDemon<EntityVengefulSpirit>(this, new ModelPlayer(.6f, true)));
		super.doRender(spirit, par2, par4, par6, par8, par9);
	}
*/
    @Override
    public void doRender(AbstractClientPlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.popMatrix();
    }
    
    public static class Factory implements IRenderFactory<AbstractClientPlayer> {
        
        @Override
        public Render<? super AbstractClientPlayer> createRenderFor(RenderManager manager) {
            return new RenderVengefulSpirit(manager);
        }
    
	
    }
}
