package com.teamdman.animus.blockentities;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.registry.AnimusBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.will.WorldDemonWillHandler;

/**
 * Block Entity for Blood Core
 * Handles periodic tree growth/spreading logic and leaf regrowth
 */
public class BlockEntityBloodCore extends BlockEntity {

    private volatile int delayCounter = 1200; // 1 minute (60 seconds * 20 ticks)
    private volatile int leafRegrowthCounter = 100; // 5 seconds default
    private volatile boolean spreading = false;
    private volatile boolean removed = false;

    public BlockEntityBloodCore(BlockPos pos, BlockState state) {
        super(AnimusBlockEntities.BLOOD_CORE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide || removed) {
            return;
        }

        // Verify the block entity is still valid
        if (level.getBlockEntity(worldPosition) != this) {
            return;
        }

        // Tree spreading counter
        delayCounter--;
        if (delayCounter <= 0) {
            // Check for corrosive will in the chunk to modify timer
            double corrosiveWill = WorldDemonWillHandler.getCurrentWill(level, worldPosition, EnumWillType.CORROSIVE);

            // Base timer from config
            int baseTimer = AnimusConfig.bloodCore.treeSpreadInterval.get();
            // More corrosive will = slower growth (up to 2x slower at 100+ will)
            double willMultiplier = 1.0 + Math.min(corrosiveWill / 100.0, 1.0);
            delayCounter = (int)(baseTimer * willMultiplier);

            if (AnimusConfig.bloodCore.debug.get()) {
                Animus.LOGGER.debug("Blood Core at {} timer expired. Spreading: {}, Next interval: {} ticks",
                    worldPosition, spreading, delayCounter);
            }

            // Attempt to spread blood trees
            if (spreading) {
                trySpreadBloodTree((ServerLevel) level);
            }

            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        // Leaf regrowth counter (only when spreading is enabled)
        if (spreading) {
            leafRegrowthCounter--;
            if (leafRegrowthCounter <= 0) {
                leafRegrowthCounter = AnimusConfig.bloodCore.leafRegrowthSpeed.get();
                tryRegrowLeaves((ServerLevel) level);
            }
        }
    }

    /**
     * Attempts to regrow missing blood leaves on this tree
     */
    private void tryRegrowLeaves(ServerLevel level) {
        RandomSource random = level.getRandom();

        // Search for blood wood logs below and around the blood core
        // This is the tree trunk
        int searchRange = 3; // Check 3 blocks in each direction
        int maxHeight = 8; // Check up to 8 blocks down

        for (int y = 0; y >= -maxHeight; y--) {
            BlockPos trunkPos = worldPosition.below(-y);
            BlockState trunkState = level.getBlockState(trunkPos);

            // If we found blood wood, check around it for missing leaves
            if (trunkState.is(AnimusBlocks.BLOCK_BLOOD_WOOD.get())) {
                // Check a small area around this trunk piece for missing leaves
                for (int x = -searchRange; x <= searchRange; x++) {
                    for (int z = -searchRange; z <= searchRange; z++) {
                        // Skip the trunk itself
                        if (x == 0 && z == 0) continue;

                        BlockPos leafPos = trunkPos.offset(x, 0, z);
                        BlockState currentState = level.getBlockState(leafPos);

                        // If it's air and reasonably close to trunk, regrow a leaf
                        if (currentState.isAir() && Math.abs(x) + Math.abs(z) <= 4) {
                            // 20% chance per attempt to regrow each leaf
                            if (random.nextFloat() < 0.2f) {
                                BlockState leafState = AnimusBlocks.BLOCK_BLOOD_LEAVES.get().defaultBlockState()
                                    .setValue(LeavesBlock.PERSISTENT, true);
                                level.setBlock(leafPos, leafState, 3);
                                return; // Only regrow one leaf per attempt
                            }
                        }
                    }
                }
            }
        }
    }

    public void trySpreadBloodTree(ServerLevel level) {
        RandomSource random = level.getRandom();

        // Search radius from config
        int searchRadius = AnimusConfig.bloodCore.treeSpreadRadius.get();
        int maxAttempts = 10;

        if (AnimusConfig.bloodCore.debug.get()) {
            Animus.LOGGER.debug("Blood Core at {} attempting to spread trees (radius: {})", worldPosition, searchRadius);
        }

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            // Pick a random position in range
            int xOffset = random.nextInt(searchRadius * 2 + 1) - searchRadius;
            int zOffset = random.nextInt(searchRadius * 2 + 1) - searchRadius;

            // Skip if too close to the blood core (within 3 blocks)
            double distance = Math.sqrt(xOffset * xOffset + zOffset * zOffset);
            if (distance < 3.0) {
                continue;
            }

            BlockPos targetPos = worldPosition.offset(xOffset, 0, zOffset);

            // Find the top solid block
            targetPos = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, targetPos);

            // Check if we can place a sapling here
            BlockState groundState = level.getBlockState(targetPos);
            BlockPos saplingPos = targetPos.above();
            BlockState aboveState = level.getBlockState(saplingPos);

            // If the heightmap gave us a grass plant, the actual ground is below it
            if (groundState.is(Blocks.SHORT_GRASS) || groundState.is(Blocks.TALL_GRASS)) {
                saplingPos = targetPos; // The grass position is where we'll place the sapling
                targetPos = targetPos.below(); // The actual ground is below
                groundState = level.getBlockState(targetPos);
                aboveState = level.getBlockState(saplingPos); // This is the grass we'll replace
            }

            if (AnimusConfig.bloodCore.debug.get()) {
                Animus.LOGGER.debug("Attempt {}: target={}, ground={}, above={}",
                    attempt, targetPos, groundState.getBlock(), aboveState.getBlock());
            }

            // Must be on grass/dirt and have air/grass above (can replace grass)
            if ((groundState.is(Blocks.GRASS_BLOCK) || groundState.is(Blocks.DIRT)) &&
                (aboveState.isAir() || aboveState.is(Blocks.SHORT_GRASS) || aboveState.is(Blocks.TALL_GRASS))) {

                // Check if there's enough space for a tree (at least 7 blocks high)
                // Can replace air, grass, and tall grass
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

                    // Place and immediately grow a blood sapling
                    level.setBlock(saplingPos, AnimusBlocks.BLOCK_BLOOD_SAPLING.get().defaultBlockState(), 3);

                    // Use bonemeal behavior to grow it
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

                    // Consume some corrosive will for the growth
                    WorldDemonWillHandler.drainWillFromChunk(level, worldPosition, EnumWillType.CORROSIVE, 5.0);

                    break; // Successfully placed one tree, stop trying
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

        // Update block state to match loaded spreading state
        if (level != null && !level.isClientSide) {
            BlockState currentState = level.getBlockState(worldPosition);
            if (currentState.getBlock() instanceof com.teamdman.animus.blocks.BlockBloodCore) {
                boolean stateActive = currentState.getValue(com.teamdman.animus.blocks.BlockBloodCore.ACTIVE);
                if (stateActive != spreading) {
                    level.setBlock(worldPosition, currentState.setValue(com.teamdman.animus.blocks.BlockBloodCore.ACTIVE, spreading), 3);
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        this.removed = true;
        // Clean up any resources here if needed
    }
}
