package com.breakinblocks.animusnv.advancements;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AnimusCriteriaTriggers {

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
        DeferredRegister.create(Registries.TRIGGER_TYPE, Constants.Mod.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, AltarTierTrigger> ALTAR_TIER =
        TRIGGERS.register("altar_tier", AltarTierTrigger::new);

    public static void register(IEventBus modEventBus) {
        TRIGGERS.register(modEventBus);
    }
}
