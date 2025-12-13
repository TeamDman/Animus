package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;


public class AnimusAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Constants.Mod.MODID);

    /**
     * Unarmed Damage attribute - adds bonus damage when attacking with empty mainhand
     * Base value: 0 (no bonus by default)
     * Min: 0, Max: 1024
     */
    public static final DeferredHolder<Attribute, Attribute> UNARMED_DAMAGE = ATTRIBUTES.register("unarmed_damage",
        () -> new RangedAttribute("attribute.animus.unarmed_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true));
}
