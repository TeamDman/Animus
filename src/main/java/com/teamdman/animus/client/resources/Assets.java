package com.teamdman.animus.client.resources;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;

import java.util.HashMap;
import java.util.Map;

public class Assets implements IResourceManagerReloadListener {
    public static Assets resReloadInstance = new Assets();
    public static boolean reloading = false;

    private static Map<Loader.SubLocation, Map<String, BindableResource>> loadedTextures = new HashMap<>();

    private Assets() {}

    public static BindableResource loadTexture(Loader.TextureLocation location, String name) {
        if(name.endsWith(".png")) {
            throw new IllegalArgumentException("Tried to loadTexture with appended .png from the AssetLibrary!");
        }
        if(!loadedTextures.containsKey(location)) {
            
        	loadedTextures.put(location, new HashMap<>());
        }
        Map<String, BindableResource> resources = loadedTextures.get(location);
        if(resources.containsKey(name)) {
            return resources.get(name);
        }
        BindableResource res = Loader.load(Loader.AssetLocation.TEXTURES, location, name, ".png");
        resources.put(name, res);
        return res;
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        if(reloading) return;
        reloading = true;
        
        for (Map<String, BindableResource> map : loadedTextures.values()) {
            for (BindableResource res : map.values()) {
                res.invalidateAndReload(); 
            }
        }
        reloading = false;

        
        TexturePreloader.doPreloadRoutine();
        
        
    }
}
