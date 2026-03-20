package com.teamdman.animus.handlers;

import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import com.breakinblocks.neovitae.common.blockentity.BloodAltarTile;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;

/**
 * Prevents the Sanguine Diviner from being placed into Blood Altars or interfering with Ritual Stones.
 * Forces the item's useOn() to handle the interaction instead of the block's use().
 */
@EventBusSubscriber(modid = Constants.Mod.MODID)
public class SanguineDivinerInteractionHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!event.getItemStack().is(AnimusItems.SANGUINE_DIVINER.get())) {
            return;
        }

        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity instanceof BloodAltarTile || blockEntity instanceof IMasterRitualStone) {
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.TRUE);
        }
    }
}
