package com.breakinblocks.animusnv.blocks;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.blockentities.BlockEntityAntiLife;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;

import java.util.ArrayList;
import java.util.List;

/**
 * AntiLife Block - Consumes nearby blocks of a specific type
 * Used by the Sigil of Consumption to convert blocks into antilife
 * Spreads to adjacent blocks of the same type, consuming EV per spread
 */
public class BlockAntiLife extends BaseEntityBlock {
    public static final MapCodec<BlockAntiLife> CODEC = simpleCodec(p -> new BlockAntiLife());
    public static final BooleanProperty DECAYING = BooleanProperty.create("decaying");

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public BlockAntiLife() {
        super(Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(0.5F)
            .noOcclusion()
            .noLootTable()
        );
        registerDefaultState(stateDefinition.any().setValue(DECAYING, false));
    }

    public static InteractionResult setBlockToAntiLife(Level level, BlockPos blockPos, Player player) {
        BlockState state = level.getBlockState(blockPos);

        if (state.is(Constants.Tags.DISALLOW_ANTILIFE)) {
            return InteractionResult.PASS;
        }

        // Check protection (e.g., FTB Chunks)
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, blockPos, state, player);
        if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) {
            return InteractionResult.PASS;
        }

        Block seeking = state.getBlock();

        level.setBlock(blockPos, AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState()
            .setValue(DECAYING, false), 3);

        if (level.getBlockEntity(blockPos) instanceof BlockEntityAntiLife antilife) {
            antilife.setSeeking(seeking).setPlayer(player);
        }

        level.scheduleTick(blockPos, AnimusBlocks.BLOCK_ANTILIFE.get(), 0);

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DECAYING);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BlockEntityAntiLife antilife)) {
            return;
        }

        boolean decaying = state.getValue(DECAYING);
        int range = antilife.getRange();

        List<BlockPos> neighbors = getNeighbors(pos);

        for (BlockPos neighborPos : neighbors) {
            BlockState neighborState = level.getBlockState(neighborPos);

            if (decaying) {
                if (neighborState.getBlock() == AnimusBlocks.BLOCK_ANTILIFE.get()) {
                    level.setBlock(neighborPos, defaultBlockState().setValue(DECAYING, true), 3);
                    level.scheduleTick(neighborPos, this, random.nextInt(10) + 10);
                    level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.01F, 0.75F);
                }
            } else if (range > 0) {
                if (!level.isEmptyBlock(neighborPos) && neighborState.getBlock() == antilife.getSeeking()) {
                    Player player = antilife.getPlayerUUID() != null ? level.getPlayerByUUID(antilife.getPlayerUUID()) : null;

                    if (player != null && player.isAlive()) {
                        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, neighborPos, neighborState, player);
                        if (NeoForge.EVENT_BUS.post(breakEvent).isCanceled()) {
                            continue;
                        }
                    }

                    level.setBlock(neighborPos, AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState()
                        .setValue(DECAYING, false), 3);

                    if (level.getBlockEntity(neighborPos) instanceof BlockEntityAntiLife neighborAntiLife) {
                        neighborAntiLife.setSeeking(antilife.getSeeking());
                        neighborAntiLife.setRange(range - 1);
                        neighborAntiLife.setPlayerUUID(antilife.getPlayerUUID());
                    }

                    level.scheduleTick(neighborPos, this, random.nextInt(25));

                    if (player != null && player.isAlive()) {
                        IAnima network = NeoVitaeAPI.getInstance().getAnima(player.getUUID());
                        AnimaTicket ticket = AnimaTicket.create(AnimusConfig.sigils.antiLifeConsumption.get());
                        network.syphonAndDamage(player, ticket);
                    }

                    level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.01F, 0.75F);
                }
            }
        }

        if (decaying) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()) {
            for (BlockPos neighborPos : getNeighbors(pos)) {
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.getBlock() == AnimusBlocks.BLOCK_ANTILIFE.get()) {
                    level.setBlock(neighborPos, defaultBlockState().setValue(DECAYING, true), 3);
                    level.scheduleTick(neighborPos, this, level.getRandom().nextInt(10) + 10);
                }
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    private List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    neighbors.add(pos.offset(x, y, z));
                }
            }
        }
        return neighbors;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityAntiLife(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
