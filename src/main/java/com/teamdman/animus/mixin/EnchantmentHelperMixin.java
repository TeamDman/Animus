package com.teamdman.animus.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for vanilla EnchantmentHelper to dynamically boost enchantment levels
 * on items marked with the AnimusEnhanced NBT tag.
 *
 * This avoids modifying actual stored enchantment data, preventing exploits
 * where inflated levels could be extracted via grindstone/disenchanting.
 *
 * Uses remap = false with multi-name arrays (official + SRG) because the Mixin AP
 * cannot resolve official Mojang mapping names to SRG names in this environment.
 * Handler method bodies are still remapped correctly by ForgeGradle's reobf task.
 */
@Mixin(value = EnchantmentHelper.class, remap = false)
public class EnchantmentHelperMixin {

    /**
     * Boost the return value of getItemEnchantmentLevel by +1 for enhanced items.
     * This covers all direct enchantment level queries: combat checks, fire aspect,
     * knockback, sweeping, and any mod that calls this method.
     *
     * SRG name: m_44843_
     */
    @Inject(method = {"getItemEnchantmentLevel", "m_44843_"}, at = @At("RETURN"), cancellable = true)
    private static void animus$boostItemEnchantmentLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        int level = cir.getReturnValue();
        if (level > 0 && stack.hasTag() && stack.getTag().getBoolean("AnimusEnhanced")) {
            cir.setReturnValue(level + 1);
        }
    }

    /**
     * Intercept runIterationOnItem to boost enchantment levels by +1 for enhanced items.
     * This covers aggregate calculations like getDamageBonus, getDamageProtection, etc.
     * that iterate all enchantments via the visitor pattern.
     *
     * When an enhanced item is detected, we cancel the original iteration and manually
     * iterate the enchantment tags, calling the visitor with boosted levels.
     *
     * Parameter order in 1.20.1: (EnchantmentVisitor, ItemStack)
     * SRG name: m_44850_
     */
    @Inject(method = {"runIterationOnItem", "m_44850_"}, at = @At("HEAD"), cancellable = true)
    private static void animus$wrapIterationForEnhanced(EnchantmentHelper.EnchantmentVisitor visitor, ItemStack stack, CallbackInfo ci) {
        if (stack.hasTag() && stack.getTag().getBoolean("AnimusEnhanced")) {
            ci.cancel();
            // Manually iterate enchantment tags with +1 boosted levels
            // This avoids re-invoking runIterationOnItem (which would cause name resolution
            // issues with remap=false) while achieving the same result.
            if (!stack.isEmpty()) {
                ListTag listtag = stack.getEnchantmentTags();
                for (int i = 0; i < listtag.size(); i++) {
                    CompoundTag compoundtag = listtag.getCompound(i);
                    ResourceLocation id = animus$getEnchantmentId(compoundtag);
                    int level = Mth.clamp(compoundtag.getInt("lvl"), 0, 255);
                    if (id != null) {
                        BuiltInRegistries.ENCHANTMENT.getOptional(id).ifPresent(
                            ench -> visitor.accept(ench, level + 1)
                        );
                    }
                }
            }
        }
    }

    /**
     * Helper to read enchantment ID from a compound tag.
     * Duplicates EnchantmentHelper.getEnchantmentId to avoid calling it through
     * a potentially misnamed reference at runtime.
     */
    @Unique
    private static ResourceLocation animus$getEnchantmentId(CompoundTag tag) {
        return ResourceLocation.tryParse(tag.getString("id"));
    }
}
