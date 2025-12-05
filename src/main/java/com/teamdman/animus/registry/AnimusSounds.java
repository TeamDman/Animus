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
    public static final RegistryObject<SoundEvent> NATURESLEACH = SOUNDS.register("naturesleach",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "naturesleach")));

    // Blood Core awakening sound
    public static final RegistryObject<SoundEvent> AWAKEN_CORE = SOUNDS.register("awaken_core",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "awaken_core")));

    // Diabolical Fungi consuming demon will
    public static final RegistryObject<SoundEvent> FUNGAL_SLURP = SOUNDS.register("fungal_slurp",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "fungal_slurp")));

    // Sigil of the Monk activation
    public static final RegistryObject<SoundEvent> NINJA_TIME = SOUNDS.register("ninja_time",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "ninja_time")));

    // Execute sound for kill effects
    public static final RegistryObject<SoundEvent> EXECUTE = SOUNDS.register("execute",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "execute")));
}
