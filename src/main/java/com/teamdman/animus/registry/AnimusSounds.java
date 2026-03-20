package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;


public class AnimusSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(Registries.SOUND_EVENT, Constants.Mod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> NATURESLEACH = SOUNDS.register("naturesleach",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "naturesleach")));

    public static final DeferredHolder<SoundEvent, SoundEvent> AWAKEN_CORE = SOUNDS.register("awaken_core",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "awaken_core")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FUNGAL_SLURP = SOUNDS.register("fungal_slurp",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "fungal_slurp")));

    public static final DeferredHolder<SoundEvent, SoundEvent> NINJA_TIME = SOUNDS.register("ninja_time",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "ninja_time")));

    public static final DeferredHolder<SoundEvent, SoundEvent> EXECUTE = SOUNDS.register("execute",
        () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "execute")));
}
