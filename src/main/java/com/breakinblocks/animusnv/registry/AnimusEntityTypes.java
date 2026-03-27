package com.breakinblocks.animusnv.registry;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.entities.EntityHellforgedArrow;
import com.breakinblocks.animusnv.entities.EntitySentientArrow;
import com.breakinblocks.animusnv.entities.EntityThrownSpear;
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

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySentientArrow>> SENTIENT_ARROW = ENTITY_TYPES.register(
        "sentient_arrow",
        () -> EntityType.Builder.<EntitySentientArrow>of(EntitySentientArrow::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("sentient_arrow")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<EntityHellforgedArrow>> HELLFORGED_ARROW = ENTITY_TYPES.register(
        "hellforged_arrow",
        () -> EntityType.Builder.<EntityHellforgedArrow>of(EntityHellforgedArrow::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("hellforged_arrow")
    );
}
