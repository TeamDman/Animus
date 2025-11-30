package com.teamdman.animus.blocks;

import com.teamdman.animus.registry.AnimusRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

/**
 * Imperfect Ritual Stone - One-time use ritual activation block
 * Right-click to activate ritual based on block placed on top
 * Consumes LP and destroys itself after use
 */
public class BlockImperfectRitualStone extends Block {

    public BlockImperfectRitualStone() {
        super(Properties.of()
            .strength(2.0F, 5.0F)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops()
        );
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        // Find matching recipe
        var recipeManager = serverLevel.getRecipeManager();
        var recipe = recipeManager.getAllRecipesFor(AnimusRecipeTypes.IMPERFECT_RITUAL_TYPE.get())
            .stream()
            .filter(r -> r.matches(aboveState))
            .findFirst();

        if (recipe.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.imperfect_stone.no_ritual", aboveState.getBlock().getName()),
                true
            );
            return InteractionResult.FAIL;
        }

        var ritualRecipe = recipe.get();

        // Get player's soul network
        SoulNetwork network = NetworkHelper.getSoulNetwork(serverPlayer);
        if (network == null) {
            return InteractionResult.FAIL;
        }

        // Check LP cost
        int lpCost = ritualRecipe.getLpCost();
        if (network.getCurrentEssence() < lpCost) {
            player.displayClientMessage(
                Component.translatable("ritual.animus.imperfect_stone.not_enough_lp", lpCost),
                true
            );
            network.causeNausea();
            return InteractionResult.FAIL;
        }

        // Consume LP
        network.syphon(new SoulTicket(
            Component.translatable("ritual.animus.imperfect_stone.ticket", ritualRecipe.getRitualKey()),
            lpCost
        ));

        // Execute ritual effect
        boolean success = ritualRecipe.onActivate(serverLevel, pos, abovePos, serverPlayer);

        if (success) {
            // Summon lightning strike at ritual stone position
            net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.spawn(
                serverLevel,
                pos,
                net.minecraft.world.entity.MobSpawnType.TRIGGERED
            );

            player.displayClientMessage(
                Component.translatable("ritual.animus.imperfect_stone.activated"),
                true
            );
        }

        return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
}
