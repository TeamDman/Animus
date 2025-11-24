package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Constants.Mod.MODID);

    // Sound events will be registered here
    // Example pattern:
    // public static final RegistryObject<SoundEvent> CUSTOM_SOUND = SOUNDS.register("custom_sound",
    //     () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.Mod.MODID, "custom_sound")));

    // Placeholder for sounds to be ported
}
