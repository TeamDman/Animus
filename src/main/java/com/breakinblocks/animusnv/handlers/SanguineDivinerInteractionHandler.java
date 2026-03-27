package com.breakinblocks.animusnv.handlers;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import com.breakinblocks.neovitae.common.blockentity.AraVitaeTile;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;

/**
 * Prevents the Sanguine Diviner from being placed into Ara Vitaes or interfering with Ritual Stones.
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
        if (blockEntity instanceof AraVitaeTile || blockEntity instanceof IMasterRitualStone) {
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.TRUE);
        }
    }
}
