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

    /**
     * Bonus Sacrifice - % increase to LP gained from mob sacrifice (Dagger of Sacrifice)
     */
    public static final DeferredHolder<Attribute, Attribute> BONUS_SACRIFICE = ATTRIBUTES.register("bonus_sacrifice",
        () -> new RangedAttribute("attribute.animus.bonus_sacrifice", 0.0D, 0.0D, 1000.0D).setSyncable(true));

    /**
     * Bonus Self-Sacrifice - % increase to LP gained from self-sacrifice (Sacrificial Dagger)
     */
    public static final DeferredHolder<Attribute, Attribute> BONUS_SELF_SACRIFICE = ATTRIBUTES.register("bonus_self_sacrifice",
        () -> new RangedAttribute("attribute.animus.bonus_self_sacrifice", 0.0D, 0.0D, 1000.0D).setSyncable(true));

    /**
     * Bonus Demon Will - % increase to demon will drops from sentient weapons
     */
    public static final DeferredHolder<Attribute, Attribute> BONUS_DEMON_WILL = ATTRIBUTES.register("bonus_demon_will",
        () -> new RangedAttribute("attribute.animus.bonus_demon_will", 0.0D, 0.0D, 1000.0D).setSyncable(true));

    /**
     * Sigil Cost Reduction - % reduction to LP cost when using sigils
     */
    public static final DeferredHolder<Attribute, Attribute> SIGIL_COST_REDUCTION = ATTRIBUTES.register("sigil_cost_reduction",
        () -> new RangedAttribute("attribute.animus.sigil_cost_reduction", 0.0D, 0.0D, 100.0D).setSyncable(true));
}
