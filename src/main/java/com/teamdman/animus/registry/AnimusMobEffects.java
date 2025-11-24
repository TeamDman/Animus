package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Constants.Mod.MODID);

    // Mob effects (potions) will be registered here
    // Example pattern:
    // public static final RegistryObject<MobEffect> CUSTOM_EFFECT = MOB_EFFECTS.register("custom_effect",
    //     () -> new CustomMobEffect(MobEffectCategory.BENEFICIAL, 0xFF0000));

    // Placeholder for potions to be ported
    // public static final RegistryObject<MobEffect> VENGEFUL_SPIRITS = ...
}
