package com.teamdman.animus.client.resources;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BindableResource {

    private ITextureObject resource = null;
    private String path;

    public BindableResource(String path) {
        this.path = path;
        allocateGlId();
    }

    public String getPath() {
        return path;
    }

    public boolean isInitialized() {
        return resource != null;
    }

    public ITextureObject getResource() {
        return resource;
    }

    public SpriteSheetResource asSpriteSheet(int columns, int rows) {
        return new SpriteSheetResource(this, rows, columns);
    }

    @Deprecated
    public void invalidateAndReload() {
        if(resource != null)
            GL11.glDeleteTextures(resource.getGlTextureId());
        resource = null;
    }

    public void allocateGlId() {
        if (resource != null || Assets.reloading) return;
        resource = new SimpleTexture(new ResourceLocation(path));
        try {
            resource.loadTexture(Minecraft.getMinecraft().getResourceManager());
        } catch (Exception exc) {
            
            exc.printStackTrace();
            resource = TextureUtil.MISSING_TEXTURE;
            return;
        }
        
    }

    public void bind() {
        if(Assets.reloading) return; //we do nothing but wait.
        if (resource == null) {
            allocateGlId();
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, resource.getGlTextureId());

    }

}
