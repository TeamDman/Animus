package com.teamdman.animus.client.resources;

public class TexturePreloader {

	public static void doPreloadRoutine() {
		TexturePreloader.preloadMandatoryTextures();

		TexturePreloader.preloadTextures();
		SpriteLibrary.init();
	}

	private static void preloadMandatoryTextures() {
		//none at this time
	}

	private static void preloadTextures() {
		Assets.loadTexture(Loader.TextureLocation.EFFECT, "culling").allocateGlId();
		Assets.loadTexture(Loader.TextureLocation.EFFECT, "naturesleech").allocateGlId();
		SpriteLibrary.init(); //Loads all spritesheets
	}
}
