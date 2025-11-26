package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import com.teamdman.animus.blocks.BlockAntimatter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.List;

/**
 * Sigil of Consumption - converts blocks to antimatter
 * Consumes LP to convert blocks into spreading antimatter that destroys matching blocks
 */
public class ItemSigilConsumption extends AnimusSigilBase {
    public ItemSigilConsumption() {
        super(Constants.Sigils.CONSUMPTION, 200);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Check if sigil is bound to a player
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

        // Raycast to find target block
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endVec = eyePos.add(lookVec.scale(5.0));

        BlockHitResult result = level.clip(new ClipContext(
            eyePos,
            endVec,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.ANY,
            player
        ));

        if (result.getType() != HitResult.Type.MISS && result.getType() == HitResult.Type.BLOCK) {
            // Consume LP from soul network
            SoulNetwork network = NetworkHelper.getSoulNetwork(player);
            SoulTicket ticket = new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_CONSUMPTION),
                getLpUsed()
            );

            var syphonResult = network.syphonAndDamage(player, ticket);
            if (!syphonResult.isSuccess()) {
                return InteractionResultHolder.fail(stack);
            }

            // Convert block to antimatter
            var antimatterResult = BlockAntimatter.setBlockToAntimatter(level, result.getBlockPos(), player);
            if (antimatterResult.consumesAction()) {
                return InteractionResultHolder.success(stack);
            }

            return InteractionResultHolder.fail(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.SIGIL_CONSUMPTION_FLAVOUR));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
