package com.breakinblocks.animusnv.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin to conditionally load EvilCraft mixins only when EvilCraft is present.
 */
public class EvilCraftMixinPlugin implements IMixinConfigPlugin {

    private boolean evilcraftLoaded = false;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            var resource = classLoader.getResource("org/cyclops/evilcraft/core/fluid/BloodFluidConverter.class");
            if (resource != null) {
                evilcraftLoaded = true;
            } else {
                Class.forName("org.cyclops.evilcraft.core.fluid.BloodFluidConverter", false, classLoader);
                evilcraftLoaded = true;
            }
        } catch (ClassNotFoundException e) {
            evilcraftLoaded = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return evilcraftLoaded;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
