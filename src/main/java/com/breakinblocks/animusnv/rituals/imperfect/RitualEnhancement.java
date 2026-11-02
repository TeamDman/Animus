package com.breakinblocks.animusnv.rituals.imperfect;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of Enhancement
 * Requires: Amethyst Block on top of Imperfect Ritual Stone
 * Cost: 5000 EV
 * Effect: Enhances all enchantments on mainhand item by 1 level (one-time only)
 */
public class RitualEnhancement extends ImperfectRitual {

    public RitualEnhancement() {
        super(
            Constants.Rituals.ENHANCEMENT,
            state -> state.is(Blocks.AMETHYST_BLOCK),
            5000,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ENHANCEMENT
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        ItemStack mainhandItem = player.getMainHandItem();
        if (mainhandItem.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animusnv.enhancement.no_item"),
                false
            );
            return false;
        }

        Boolean enhanced = mainhandItem.get(AnimusDataComponents.ANIMUS_ENHANCED.get());
        if (enhanced != null && enhanced) {
            player.displayClientMessage(
                Component.translatable("ritual.animusnv.enhancement.already_enhanced"),
                false
            );
            return false;
        }

        ItemEnchantments enchantments = mainhandItem.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animusnv.enhancement.no_enchantments"),
                false
            );
            return false;
        }

        ItemEnchantments enhancedEnchantments = enhance(enchantments);
        if (enhancedEnchantments.equals(enchantments)) {
            player.displayClientMessage(
                Component.translatable("ritual.animusnv.enhancement.at_limit"),
                false
            );
            return false;
        }

        mainhandItem.set(DataComponents.ENCHANTMENTS, enhancedEnchantments);
        mainhandItem.set(AnimusDataComponents.ANIMUS_ENHANCED.get(), true);

        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.5F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animusnv.enhancement.success"),
            true
        );

        return true;
    }

    public static ItemEnchantments enhance(ItemEnchantments enchantments) {
        ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(enchantments);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            int currentLevel = enchantments.getLevel(enchantment);
            if (currentLevel <= enchantment.value().getMaxLevel()) {
                mutableEnchantments.set(enchantment, currentLevel + 1);
            }
        }
        return mutableEnchantments.toImmutable();
    }
}
