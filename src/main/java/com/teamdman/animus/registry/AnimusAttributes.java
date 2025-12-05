package com.teamdman.animus.registry;

import com.teamdman.animus.Constants;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AnimusAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Constants.Mod.MODID);

    /**
     * Unarmed Damage attribute - adds bonus damage when attacking with empty mainhand
     * Base value: 0 (no bonus by default)
     * Min: 0, Max: 1024
     */
    public static final RegistryObject<Attribute> UNARMED_DAMAGE = ATTRIBUTES.register("unarmed_damage",
        () -> new RangedAttribute("attribute.animus.unarmed_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true));
}
