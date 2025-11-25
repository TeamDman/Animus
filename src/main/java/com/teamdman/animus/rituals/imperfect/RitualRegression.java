package com.teamdman.animus.rituals.imperfect;

import com.teamdman.animus.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import wayoftime.bloodmagic.ritual.RitualRegister;
import wayoftime.bloodmagic.ritual.imperfect.IImperfectRitualStone;
import wayoftime.bloodmagic.ritual.imperfect.ImperfectRitual;

/**
 * Imperfect Ritual of Regression
 * <p>
 * Requires: Bookshelf on top of Imperfect Ritual Stone
 * Cost: 3000 LP
 * Effect: Removes the repair cost from the held item (anvil penalty reset)
 */
@RitualRegister.Imperfect(Constants.Rituals.REGRESSION)
public class RitualRegression extends ImperfectRitual {

    public RitualRegression() {
        super(
            Constants.Rituals.REGRESSION,
            state -> state.is(Blocks.BOOKSHELF),
            3000,
            true,
            "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.REGRESSION
        );
    }

    @Override
    public boolean onActivate(IImperfectRitualStone ritualStone, Player player) {
        Level level = ritualStone.getRitualWorld();

        if (level.isClientSide) {
            return false;
        }

        // Check if player is holding an item
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            // Send message to player
            player.displayClientMessage(
                Component.translatable("text.component.holdingitem"),
                false
            );
            return false;
        }

        // Remove repair cost from item
        CompoundTag tag = heldItem.getOrCreateTag();
        tag.remove("RepairCost");

        return true;
    }
}
