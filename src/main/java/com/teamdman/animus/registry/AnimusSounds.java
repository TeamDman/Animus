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

    // Nature's Leech sound effect
    public static final RegistryObject<SoundEvent> NATURESLEECH = SOUNDS.register("naturesleech",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.Mod.MODID, "naturesleech")));

    // Additional sounds can be registered here
}
