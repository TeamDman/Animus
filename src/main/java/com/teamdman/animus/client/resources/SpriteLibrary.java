package com.teamdman.animus.client.resources;


public class SpriteLibrary {
    private static final BindableResource texCulling = Assets.loadTexture(Loader.TextureLocation.EFFECT, "culling");
    public static final SpriteSheetResource spriteCulling = texCulling.asSpriteSheet(8, 8);
    		

    public static void init() {}

}
