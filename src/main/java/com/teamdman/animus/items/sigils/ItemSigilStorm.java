package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Sigil of the Storm - summons lightning
 * TODO: Implement full Blood Magic integration:
 * - Check player's soul network for LP
 * - Consume LP on use
 * - Add fish spawning in water
 * - Add area damage during rain
 * - Implement binding system
 */
public class ItemSigilStorm extends ItemSigilBase {

    public ItemSigilStorm() {
        super(Constants.Sigils.STORM, 500);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // TODO: Check if player has binding
        // TODO: Check if player has enough LP (500)

        // Raycast to find target position
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endVec = eyePos.add(lookVec.scale(64.0));

        BlockHitResult result = level.clip(new net.minecraft.world.level.ClipContext(
            eyePos,
            endVec,
            net.minecraft.world.level.ClipContext.Block.OUTLINE,
            net.minecraft.world.level.ClipContext.Fluid.ANY,
            player
        ));

        if (result.getType() != HitResult.Type.MISS) {
            BlockPos pos = result.getBlockPos();

            // Spawn lightning
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning != null) {
                lightning.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                lightning.setVisualOnly(false);
                level.addFreshEntity(lightning);
            }

            // TODO: Implement fish spawning if targeting water
            // TODO: Implement area damage if raining
            // TODO: Consume LP from soul network

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }
}
