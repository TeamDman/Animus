package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntityThrownSpear;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;


public class AnimusEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, Constants.Mod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityThrownSpear>> THROWN_PILUM = ENTITY_TYPES.register(
        "thrown_spear",
        () -> EntityType.Builder.<EntityThrownSpear>of(EntityThrownSpear::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("thrown_spear")
    );
}
