package com.teamdman.animus.handlers;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public final class AnimusSoundEventHandler {

	public static final SoundEvent ghostly = getRegisteredSoundEvent("animus:ghostly");
	public static final SoundEvent naturesleech = getRegisteredSoundEvent("animus:naturesleech");

	private static SoundEvent getRegisteredSoundEvent(String name) {
		return SoundEvent.REGISTRY.getObject(new ResourceLocation(name));
	}

	private AnimusSoundEventHandler() {
	}

}
