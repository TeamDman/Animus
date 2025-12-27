package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import com.teamdman.animus.entities.EntityHellforgedArrow;
import com.teamdman.animus.entities.EntitySentientArrow;
import com.teamdman.animus.entities.EntityThrownSpear;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Constants.Mod.MODID);

    public static final RegistryObject<EntityType<EntityThrownSpear>> THROWN_PILUM = ENTITY_TYPES.register(
        "thrown_spear",
        () -> EntityType.Builder.<EntityThrownSpear>of(EntityThrownSpear::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("thrown_spear")
    );

    public static final RegistryObject<EntityType<EntitySentientArrow>> SENTIENT_ARROW = ENTITY_TYPES.register(
        "sentient_arrow",
        () -> EntityType.Builder.<EntitySentientArrow>of(EntitySentientArrow::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("sentient_arrow")
    );

    public static final RegistryObject<EntityType<EntityHellforgedArrow>> HELLFORGED_ARROW = ENTITY_TYPES.register(
        "hellforged_arrow",
        () -> EntityType.Builder.<EntityHellforgedArrow>of(EntityHellforgedArrow::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build("hellforged_arrow")
    );
}
