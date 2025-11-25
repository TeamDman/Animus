package com.teamdman.animus.worldgen;

import com.teamdman.animus.Constants;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry for custom tree decorator types
 */
public class AnimusTreeDecoratorTypes {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES =
        DeferredRegister.create(ForgeRegistries.TREE_DECORATOR_TYPES, Constants.Mod.MODID);

    public static final RegistryObject<TreeDecoratorType<BloodCoreDecorator>> BLOOD_CORE =
        TREE_DECORATOR_TYPES.register("blood_core",
            () -> new TreeDecoratorType<>(BloodCoreDecorator.CODEC));
}
