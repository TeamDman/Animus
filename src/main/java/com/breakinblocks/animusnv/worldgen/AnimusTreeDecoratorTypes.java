package com.breakinblocks.animusnv.worldgen;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AnimusTreeDecoratorTypes {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES =
        DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, Constants.Mod.MODID);

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<BloodCoreDecorator>> BLOOD_CORE =
        TREE_DECORATOR_TYPES.register("blood_core",
            () -> new TreeDecoratorType<>(BloodCoreDecorator.CODEC));
}
