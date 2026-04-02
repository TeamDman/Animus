package com.breakinblocks.animusnv.blockentities;

import com.breakinblocks.animusnv.Animus;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
import com.breakinblocks.animusnv.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.api.NeoVitaeAPI;
import com.breakinblocks.neovitae.api.will.ISpiritusHandler;

/**
 * Block Entity for Blood Core
 * Handles periodic tree growth/spreading logic and leaf regrowth
 */
public class BlockEntityBloodCore extends BlockEntity {

    private volatile int delayCounter = 1200;
    private volatile int leafRegrowthCounter = 100;
    private volatile boolean spreading = false;
    private volatile boolean removed = false;

    public BlockEntityBloodCore(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.BLOOD_CORE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide || removed) {
            return;
        }

        if (level.getBlockEntity(worldPosition) != this) {
            return;
        }

        delayCounter--;
        if (delayCounter <= 0) {
            ISpiritusHandler willHandler = NeoVitaeAPI.getInstance().getSpiritusHandler();
            double corrosiveWill = willHandler.getCurrentWill(level, worldPosition, SpiritusType.CORROSIVE);

            int baseTimer = AnimusConfig.bloodCore.treeSpreadInterval.get();
            // More corrosive will = slower growth (up to 2x slower at 100+ will)
            double willMultiplier = 1.0 + Math.min(corrosiveWill / 100.0, 1.0);
            delayCounter = (int)(baseTimer * willMultiplier);

            if (AnimusConfig.bloodCore.debug.get()) {
                Animus.LOGGER.debug("Blood Core at {} timer expired. Spreading: {}, Next interval: {} ticks",
                    worldPosition, spreading, delayCounter);
            }

            if (spreading) {
                trySpreadBloodTree((ServerLevel) level);
            }

            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        if (spreading) {
            leafRegrowthCounter--;
            if (leafRegrowthCounter <= 0) {
                leafRegrowthCounter = AnimusConfig.bloodCore.leafRegrowthSpeed.get();
                tryRegrowTrunk((ServerLevel) level);
                tryRegrowLeaves((ServerLevel) level);
            }
        }
    }

    /**
     * Attempts to regrow missing blood wood logs in the trunk below the core.
     * Scans straight down and fills air gaps with blood wood.
     */
    private void tryRegrowTrunk(ServerLevel level) {
        RandomSource random = level.getRandom();
        int maxHeight = 8;

        for (int dy = 1; dy <= maxHeight; dy++) {
            BlockPos checkPos = worldPosition.below(dy);
            BlockState state = level.getBlockState(checkPos);

            if (state.is(AnimusBlocks.BLOCK_BLOOD_WOOD.get())) {
                continue;
            }

            if (state.isAir() || state.is(AnimusBlocks.BLOCK_BLOOD_LEAVES.get())) {
                if (random.nextFloat() < 0.3f) {
                    level.setBlock(checkPos, AnimusBlocks.BLOCK_BLOOD_WOOD.get().defaultBlockState(), 3);
                    return;
                }
            } else {
                break;
            }
        }
    }

    private void tryRegrowLeaves(ServerLevel level) {
        RandomSource random = level.getRandom();

        int searchRange = 3;
        int maxHeight = 8;

        for (int y = 0; y >= -maxHeight; y--) {
            BlockPos trunkPos = worldPosition.below(-y);
            BlockState trunkState = level.getBlockState(trunkPos);

            if (trunkState.is(AnimusBlocks.BLOCK_BLOOD_WOOD.get())) {
                for (int x = -searchRange; x <= searchRange; x++) {
                    for (int z = -searchRange; z <= searchRange; z++) {
                        if (x == 0 && z == 0) continue;

                        BlockPos leafPos = trunkPos.offset(x, 0, z);
                        BlockState currentState = level.getBlockState(leafPos);

                        if (currentState.isAir() && Math.abs(x) + Math.abs(z) <= 4) {
                            if (random.nextFloat() < 0.2f) {
                                BlockState leafState = AnimusBlocks.BLOCK_BLOOD_LEAVES.get().defaultBlockState()
                                    .setValue(LeavesBlock.PERSISTENT, true);
                                level.setBlock(leafPos, leafState, 3);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public void trySpreadBloodTree(ServerLevel level) {
        RandomSource random = level.getRandom();

        int searchRadius = AnimusConfig.bloodCore.treeSpreadRadius.get();
        int maxAttempts = 10;

        if (AnimusConfig.bloodCore.debug.get()) {
            Animus.LOGGER.debug("Blood Core at {} attempting to spread trees (radius: {})", worldPosition, searchRadius);
        }

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int xOffset = random.nextInt(searchRadius * 2 + 1) - searchRadius;
            int zOffset = random.nextInt(searchRadius * 2 + 1) - searchRadius;

            double distance = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
            if (distance < 3.0) {
                continue;
            }

            BlockPos targetPos = worldPosition.offset(xOffset, 0, zOffset);

            targetPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, targetPos);

            BlockState groundState = level.getBlockState(targetPos);
            BlockPos saplingPos = targetPos.above();
            BlockState aboveState = level.getBlockState(saplingPos);

            // If heightmap landed on grass, adjust positions
            if (groundState.is(Blocks.SHORT_GRASS) || groundState.is(Blocks.TALL_GRASS)) {
                saplingPos = targetPos;
                targetPos = targetPos.below();
                groundState = level.getBlockState(targetPos);
                aboveState = level.getBlockState(saplingPos);
            }

            if (AnimusConfig.bloodCore.debug.get()) {
                Animus.LOGGER.debug("Attempt {}: target={}, ground={}, above={}",
                    attempt, targetPos, groundState.getBlock(), aboveState.getBlock());
            }

            if ((groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT)) &&
                (aboveState.isAir() || aboveState.is(Blocks.SHORT_GRASS) || aboveState.is(Blocks.TALL_GRASS))) {

                boolean hasSpace = true;
                for (int y = 0; y < 7; y++) {
                    BlockState checkState = level.getBlockState(saplingPos.above(y));
                    if (!checkState.isAir() && !checkState.is(Blocks.SHORT_GRASS) && !checkState.is(Blocks.TALL_GRASS)) {
                        hasSpace = false;
                        if (AnimusConfig.bloodCore.debug.get()) {
                            Animus.LOGGER.debug("  No space at y={}, block={}", y, checkState.getBlock());
                        }
                        break;
                    }
                }

                if (hasSpace) {
                    if (AnimusConfig.bloodCore.debug.get()) {
                        Animus.LOGGER.info("Blood Core at {} spawning blood tree at {}", worldPosition, saplingPos);
                    }

                    level.setBlock(saplingPos, AnimusBlocks.BLOCK_BLOOD_SAPLING.get().defaultBlockState(), 3);

                    BlockState saplingState = level.getBlockState(saplingPos);
                    if (saplingState.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock saplingBlock) {
                        saplingBlock.advanceTree(level, saplingPos, saplingState, random);
                        if (AnimusConfig.bloodCore.debug.get()) {
                            Animus.LOGGER.debug("  Tree grown successfully");
                        }
                    } else {
                        if (AnimusConfig.bloodCore.debug.get()) {
                            Animus.LOGGER.warn("  Failed to grow tree - block is not a sapling: {}", saplingState.getBlock());
                        }
                    }

                    NeoVitaeAPI.getInstance().getSpiritusHandler().drainWill(level, worldPosition, SpiritusType.CORROSIVE, 5.0);

                    break;
                }
            }
        }
    }

    public boolean isSpreading() {
        return spreading;
    }

    public void setSpreading(boolean spreading) {
        this.spreading = spreading;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("DelayCounter", delayCounter);
        tag.putInt("LeafRegrowthCounter", leafRegrowthCounter);
        tag.putBoolean("Spreading", spreading);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        delayCounter = tag.getInt("DelayCounter");
        leafRegrowthCounter = tag.getInt("LeafRegrowthCounter");
        spreading = tag.getBoolean("Spreading");

        if (level != null && !level.isClientSide) {
            BlockState currentState = level.getBlockState(worldPosition);
            if (currentState.getBlock() instanceof com.breakinblocks.animusnv.blocks.BlockBloodCore) {
                boolean stateActive = currentState.getValue(com.breakinblocks.animusnv.blocks.BlockBloodCore.ACTIVE);
                if (stateActive != spreading) {
                    level.setBlock(worldPosition, currentState.setValue(com.breakinblocks.animusnv.blocks.BlockBloodCore.ACTIVE, spreading), 3);
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.removed = true;
    }
}
