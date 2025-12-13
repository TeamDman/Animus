package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
// RegistryObject replaced by DeferredHolder

public class AnimusMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(Registries.MOB_EFFECT, Constants.Mod.MODID);

    // Mob effects (potions) will be registered here
    // Example pattern:
    // public static final DeferredHolder<MobEffect, MobEffect> CUSTOM_EFFECT = MOB_EFFECTS.register("custom_effect",
    //     () -> new CustomMobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000));

    // Placeholder for potions to be ported
    // public static final DeferredHolder<MobEffect, MobEffect> VENGEFUL_SPIRITS = ...
}
