package com.teamdman.animus.items.sigils;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Sigil of the Storm - summons lightning
 * When targeting water, spawns fish
 * When raining, deals area damage to nearby entities
 */
public class ItemSigilStorm extends AnimusSigilBase {

    public ItemSigilStorm() {
        super(Constants.Sigils.STORM, 500);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        // Check binding
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return InteractionResultHolder.fail(stack);
        }

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

            // Consume LP from soul network
            wayoftime.bloodmagic.core.data.SoulNetwork network = wayoftime.bloodmagic.util.helper.NetworkHelper.getSoulNetwork(player);
            wayoftime.bloodmagic.core.data.SoulTicket ticket = new wayoftime.bloodmagic.core.data.SoulTicket(
                net.minecraft.network.chat.Component.translatable(Constants.Localizations.Text.TICKET_STORM),
                getLpUsed()
            );

            var syphonResult = network.syphonAndDamage(player, ticket);
            if (!syphonResult.isSuccess()) {
                return InteractionResultHolder.fail(stack);
            }

            // Spawn lightning
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning != null) {
                lightning.moveTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                lightning.setVisualOnly(false);
                level.addFreshEntity(lightning);
            }

            // Fish spawning when targeting water
            if (level instanceof ServerLevel serverLevel) {
                // Check if the target block is water
                if (level.getFluidState(pos).is(Fluids.WATER) || level.getBlockState(pos).is(Blocks.WATER)) {
                    // Spawn 1-3 fish
                    int fishCount = 1 + level.random.nextInt(3);
                    for (int i = 0; i < fishCount; i++) {
                        Cod fish = EntityType.COD.create(level);
                        if (fish != null) {
                            // Spawn fish near the target position
                            double offsetX = (level.random.nextDouble() - 0.5) * 2.0;
                            double offsetZ = (level.random.nextDouble() - 0.5) * 2.0;
                            fish.moveTo(pos.getX() + 0.5 + offsetX, pos.getY() + 1, pos.getZ() + 0.5 + offsetZ);
                            serverLevel.addFreshEntity(fish);
                        }
                    }
                }

                // Area damage during rain
                if (level.isRaining() && level.canSeeSky(pos)) {
                    // Deal damage to entities in a 5 block radius
                    AABB damageArea = new AABB(pos).inflate(5.0);
                    level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, damageArea).forEach(entity -> {
                        if (entity != player && level.canSeeSky(entity.blockPosition())) {
                            entity.hurt(level.damageSources().lightningBolt(), 4.0F);
                        }
                    });
                }
            }

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(stack);
    }
}
