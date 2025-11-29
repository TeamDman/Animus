package com.teamdman.animus.handlers;

import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wayoftime.bloodmagic.common.tile.TileAltar;
import wayoftime.bloodmagic.ritual.IMasterRitualStone;

/**
 * Prevents the Sanguine Diviner from being placed into Blood Altars or interfering with Ritual Stones
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        if (blockEntity instanceof TileAltar || blockEntity instanceof IMasterRitualStone) {
            // Allow the item's useOn() method to handle this interaction
            // by setting the result to ALLOW, which prevents the block's use() method from running
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.ALLOW);
        }
    }
}
