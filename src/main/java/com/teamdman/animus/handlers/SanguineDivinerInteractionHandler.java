package com.teamdman.animus.handlers;

import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import wayoftime.bloodmagic.common.blockentity.BloodAltarTile;
import wayoftime.bloodmagic.ritual.IMasterRitualStone;

/**
 * Prevents the Sanguine Diviner from being placed into Blood Altars or interfering with Ritual Stones
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class SanguineDivinerInteractionHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        // Check if the player is holding the Sanguine Diviner
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!event.getItemStack().is(AnimusItems.SANGUINE_DIVINER.get())) {
            return;
        }

        // Check if the clicked block is a Blood Altar or Master Ritual Stone
        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity instanceof BloodAltarTile || blockEntity instanceof IMasterRitualStone) {
            // Allow the item's useOn() method to handle this interaction
            // by setting the result to ALLOW, which prevents the block's use() method from running
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.TRUE);
        }
    }
}
