package com.teamdman.animus.blocks;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.blockentities.BlockEntityAntiLife;
import com.teamdman.animus.registry.AnimusBlocks;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * AntiLife Block - Consumes nearby blocks of a specific type
 * Used by the Sigil of Consumption to convert blocks into antilife
 * Spreads to adjacent blocks of the same type, consuming LP per spread
 */
public class BlockAntiLife extends BaseEntityBlock {
    public static final BooleanProperty DECAYING = BooleanProperty.create("decaying");

    public BlockAntiLife() {
        super(Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .strength(0.5F)
            .noOcclusion()
            .noLootTable()
        );
        registerDefaultState(stateDefinition.any().setValue(DECAYING, false));
    }

    /**
     * Converts a block at the given position to antilife
     * @param level The level
     * @param blockPos Position of block to convert
     * @param player Player using the sigil (for LP consumption)
     * @return SUCCESS if converted, PASS if cannot convert
     */
    public static InteractionResult setBlockToAntiLife(Level level, BlockPos blockPos, Player player) {
        BlockState state = level.getBlockState(blockPos);

        // Don't convert blocks in the disallow_antilife tag
        if (state.is(Constants.Tags.DISALLOW_ANTILIFE)) {
            return InteractionResult.PASS;
        }

        // Fire break event to check if protected (e.g., FTB Chunks)
        BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, blockPos, state, player);
        if (MinecraftForge.EVENT_BUS.post(breakEvent)) {
            return InteractionResult.PASS; // Protected, cannot convert
        }

        Block seeking = state.getBlock();

        // Set to antilife
        level.setBlock(blockPos, AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState()
            .setValue(DECAYING, false), 3);

        // Configure block entity
        if (level.getBlockEntity(blockPos) instanceof BlockEntityAntiLife antilife) {
            antilife.setSeeking(seeking).setPlayer(player);
        }

        // Schedule first tick
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

        // Get neighbors (3x3x3 cube around this block)
        List<BlockPos> neighbors = getNeighbors(pos);

        for (BlockPos neighborPos : neighbors) {
            BlockState neighborState = level.getBlockState(neighborPos);

            if (decaying) {
                // If decaying, ALWAYS spread decay to adjacent antilife (ignore range)
                if (neighborState.getBlock() == AnimusBlocks.BLOCK_ANTILIFE.get()) {
                    level.setBlock(neighborPos, defaultBlockState().setValue(DECAYING, true), 3);
                    level.scheduleTick(neighborPos, this, random.nextInt(10) + 10);
                    level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.01F, 0.75F);
                }
            } else if (range > 0) {
                // Only spread conversion if we have range remaining
                // If not decaying, spread to matching blocks
                if (!level.isEmptyBlock(neighborPos) && neighborState.getBlock() == antilife.getSeeking()) {
                    // Fire break event to check if protected
                    Player player = antilife.getPlayerUUID() != null ? level.getPlayerByUUID(antilife.getPlayerUUID()) : null;
                    BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(level, neighborPos, neighborState, player);
                    if (MinecraftForge.EVENT_BUS.post(breakEvent)) {
                        continue; // Protected, skip this block
                    }

                    // Set neighbor to antilife
                    level.setBlock(neighborPos, AnimusBlocks.BLOCK_ANTILIFE.get().defaultBlockState()
                        .setValue(DECAYING, false), 3);

                    // Configure neighbor's block entity
                    if (level.getBlockEntity(neighborPos) instanceof BlockEntityAntiLife neighborAntiLife) {
                        neighborAntiLife.setSeeking(antilife.getSeeking());
                        neighborAntiLife.setRange(range - 1);
                        neighborAntiLife.setPlayerUUID(antilife.getPlayerUUID());
                    }

                    // Schedule neighbor tick
                    level.scheduleTick(neighborPos, this, random.nextInt(25));

                    // Consume LP from player
                    if (player != null) {
                        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
                        SoulTicket ticket = new SoulTicket(
                            Component.translatable(Constants.Localizations.Text.TICKET_ANTILIFE),
                            AnimusConfig.sigils.antiLifeConsumption.get()
                        );
                        network.syphonAndDamage(player, ticket);
                    }

                    level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.01F, 0.75F);
                }
            }
        }

        // If decaying, remove this block
        if (decaying) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        // When broken, start decay on adjacent antilife
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

    /**
     * Get all neighboring positions in a 3x3x3 cube
     */
    private List<BlockPos> getNeighbors(BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue; // Skip center
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
