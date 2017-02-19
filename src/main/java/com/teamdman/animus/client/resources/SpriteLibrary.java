package com.teamdman.animus.client.resources;


public class SpriteLibrary {
	private static final BindableResource texCulling = Assets.loadTexture(Loader.TextureLocation.EFFECT, "culling");
	private static final BindableResource texNaturesLeech = Assets.loadTexture(Loader.TextureLocation.EFFECT, "naturesleech");
	public static final SpriteSheetResource spriteCulling = texCulling.asSpriteSheet(8, 8);
	public static final SpriteSheetResource spriteNaturesLeech = texNaturesLeech.asSpriteSheet(8, 4);


	public static void init() {
	}

}
