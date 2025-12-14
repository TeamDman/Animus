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
import wayoftime.bloodmagic.api.ritual.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of Reduction
 * Requires: Quartz Block on top of Imperfect Ritual Stone
 * Cost: 1000 LP
 * Effect: Removes enhancement record and downgrades all enchantments by 1 level (min level 1)
 */
public class RitualReduction extends ImperfectRitual {

    public RitualReduction() {
        super(
            Constants.Rituals.REDUCTION,
            state -> state.is(Blocks.QUARTZ_BLOCK),
            1000,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.REDUCTION
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        // Check if player is holding an item in mainhand
        ItemStack mainhandItem = player.getMainHandItem();
        if (mainhandItem.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.reduction.no_item"),
                true
            );
            return false;
        }

        // Get enchantments using 1.21 API
        ItemEnchantments enchantments = mainhandItem.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || enchantments.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.reduction.no_enchantments"),
                true
            );
            return false;
        }

        // Downgrade all enchantments by 1 level (minimum level 1)
        ItemEnchantments.Mutable mutableEnchantments = new ItemEnchantments.Mutable(enchantments);
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            int currentLevel = enchantments.getLevel(enchantment);
            int newLevel = Math.max(1, currentLevel - 1);
            mutableEnchantments.set(enchantment, newLevel);
        }

        // Apply downgraded enchantments
        mainhandItem.set(DataComponents.ENCHANTMENTS, mutableEnchantments.toImmutable());

        // Remove enhanced marker if present
        mainhandItem.remove(AnimusDataComponents.ANIMUS_ENHANCED.get());

        // Play success sound
        level.playSound(
            null,
            player.getX(), player.getY(), player.getZ(),
            SoundEvents.GRINDSTONE_USE,
            SoundSource.BLOCKS,
            1.0F,
            0.8F
        );

        player.displayClientMessage(
            Component.translatable("ritual.animus.reduction.success"),
            true
        );

        return true;
    }
}
