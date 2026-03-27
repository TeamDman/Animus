package com.breakinblocks.animusnv.rituals.imperfect;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import com.breakinblocks.neovitae.api.ritual.IImperfectRitualStone;
import com.breakinblocks.neovitae.ritual.ImperfectRitual;

/**
 * Imperfect Ritual of Regression
 * <p>
 * Requires: Bookshelf on top of Imperfect Ritual Stone
 * Cost: 3000 EV
 * Effect: Removes the repair cost from the held item (anvil penalty reset)
 */
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

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("text.component.holdingitem"),
                false
            );
            return false;
        }

        heldItem.set(DataComponents.REPAIR_COST, 0);

        return true;
    }
}
