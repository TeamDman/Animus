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
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "naturesleech")));

    // Blood Core awakening sound
    public static final RegistryObject<SoundEvent> AWAKEN_CORE = SOUNDS.register("awaken_core",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "awaken_core")));

    // Additional sounds can be registered here
}
