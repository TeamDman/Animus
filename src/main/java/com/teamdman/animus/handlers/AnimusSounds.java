package com.teamdman.animus.handlers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class AnimusSounds {
	public static void init() {
		String[] sounds = {
				"animus:ghostly"
			};

		for (String s : sounds) {
			ResourceLocation location = new ResourceLocation(s);
			GameRegistry.register(new SoundEvent(location), location);
		}
	}

	private AnimusSounds() {}
}
