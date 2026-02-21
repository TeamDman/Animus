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

    /**
     * Bonus Sacrifice attribute - % increase to LP gained from mob sacrifice (Dagger of Sacrifice)
     * Value interpreted as percentage (e.g. 50 = 50% bonus)
     */
    public static final RegistryObject<Attribute> BONUS_SACRIFICE = ATTRIBUTES.register("bonus_sacrifice",
        () -> new RangedAttribute("attribute.animus.bonus_sacrifice", 0.0D, 0.0D, 1000.0D).setSyncable(true));

    /**
     * Bonus Self-Sacrifice attribute - % increase to LP gained from self-sacrifice (Sacrificial Dagger)
     * Value interpreted as percentage (e.g. 50 = 50% bonus)
     */
    public static final RegistryObject<Attribute> BONUS_SELF_SACRIFICE = ATTRIBUTES.register("bonus_self_sacrifice",
        () -> new RangedAttribute("attribute.animus.bonus_self_sacrifice", 0.0D, 0.0D, 1000.0D).setSyncable(true));

    /**
     * Bonus Demon Will attribute - % increase to demon will drops from sentient weapons
     * Value interpreted as percentage (e.g. 100 = 100% bonus = double will)
     */
    public static final RegistryObject<Attribute> BONUS_DEMON_WILL = ATTRIBUTES.register("bonus_demon_will",
        () -> new RangedAttribute("attribute.animus.bonus_demon_will", 0.0D, 0.0D, 1000.0D).setSyncable(true));

    /**
     * Sigil Cost Reduction attribute - % reduction to LP cost when using sigils
     * Value interpreted as percentage (e.g. 50 = 50% cost reduction)
     */
    public static final RegistryObject<Attribute> SIGIL_COST_REDUCTION = ATTRIBUTES.register("sigil_cost_reduction",
        () -> new RangedAttribute("attribute.animus.sigil_cost_reduction", 0.0D, 0.0D, 100.0D).setSyncable(true));
}
