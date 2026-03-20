package com.teamdman.animus.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin to conditionally load Iron's Spells mixins only when the mod is present.
 */
public class IronsSpellsMixinPlugin implements IMixinConfigPlugin {

    private boolean ironsSpellsLoaded = false;

    @Override
    public void onLoad(String mixinPackage) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = getClass().getClassLoader();
            }

            var resource = classLoader.getResource("io/redspace/ironsspellbooks/api/spells/AbstractSpell.class");
            if (resource != null) {
                ironsSpellsLoaded = true;
            } else {
                Class.forName("io.redspace.ironsspellbooks.api.spells.AbstractSpell", false, classLoader);
                ironsSpellsLoaded = true;
            }
        } catch (ClassNotFoundException e) {
            ironsSpellsLoaded = false;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("AbstractSpellMixin")) {
            return ironsSpellsLoaded;
        }
        return true;
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
