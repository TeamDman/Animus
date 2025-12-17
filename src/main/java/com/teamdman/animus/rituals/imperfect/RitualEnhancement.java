package com.teamdman.animus.rituals.imperfect;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusDataComponents;
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
 * Cost: 5000 LP
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

        // Server-side only - Blood Magic handles client check in performRitual
        if (level.isClientSide) {
            return false;
        }

        // Check if player is holding an item in mainhand
        ItemStack mainhandItem = player.getMainHandItem();
        if (mainhandItem.isEmpty()) {
            // Send to chat (false) so it doesn't get overwritten by Blood Magic's action bar message
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.no_item"),
                false
            );
            return false;
        }

        // Check if item has already been enhanced using data component
        Boolean enhanced = mainhandItem.get(AnimusDataComponents.ANIMUS_ENHANCED.get());
        if (enhanced != null && enhanced) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.already_enhanced"),
                false
            );
            return false;
        }

        // Get enchantments using 1.21 API
        ItemEnchantments enchantments = mainhandItem.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.enhancement.no_enchantments"),
                false
            );
            return false;
        }

        // Build new enchantments with +1 level each
        ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(enchantments);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            int currentLevel = enchantments.getLevel(enchantment);
            mutableEnchantments.set(enchantment, currentLevel + 1);
        }

        // Apply enhanced enchantments
        mainhandItem.set(DataComponents.ENCHANTMENTS, mutableEnchantments.toImmutable());

        // Mark as enhanced
        mainhandItem.set(AnimusDataComponents.ANIMUS_ENHANCED.get(), true);

        // Play success sound
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundSource.BLOCKS,
            1.0F,
            1.5F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.enhancement.success"),
            true
        );

        return true;
    }
}
