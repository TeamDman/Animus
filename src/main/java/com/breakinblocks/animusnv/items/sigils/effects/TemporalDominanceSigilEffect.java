package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.network.AcceleratedBlocksSyncPayload;
import com.breakinblocks.animusnv.network.AnimusPayloads;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sigil of Temporal Dominance - Accelerates tick rate of targeted block entities using EV.
 * <p>
 * Progression:
 * - Level 1: 2x speed, 1000 EV, 30 seconds
 * - Level 2: 4x speed, 2000 EV, 30 seconds
 * - Level 3: 8x speed, 4000 EV, 30 seconds
 * - Level 4: 16x speed, 8000 EV, 30 seconds
 * - Level 5: 32x speed, 16000 EV, 30 seconds
 * - Level 5+: 32x speed, 15000 EV to refresh timer
 * <p>
 * When timer expires, all calculations reset.
 * Requires sneak + right-click to activate on block entities.
 */
public record TemporalDominanceSigilEffect() implements ISigilEffect {
    public static final MapCodec<TemporalDominanceSigilEffect> CODEC = MapCodec.unit(TemporalDominanceSigilEffect::new);

    // Maximum acceleration level (32x speed = 2^5)
    private static final int MAX_LEVEL = 5;
    // Base EV cost for first activation
    private static final int BASE_EV_COST = 1000;
    // Duration in ticks (30 seconds = 600 ticks)
    private static final int DURATION_TICKS = 600;
    // Refresh cost when at max level
    private static final int REFRESH_COST = 15000;

    // Track acceleration state for each block entity
    private static final Map<BlockPos, AccelerationState> acceleratedBlocks = new ConcurrentHashMap<>();

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public boolean useOnBlock(Level level, Player player, ItemStack stack, BlockPos pos, Direction side, Vec3 hitVec) {
        if (level.isClientSide || !player.isShiftKeyDown()) {
            return false;
        }

        // Check if there's a block entity at this position
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TEMPORAL_NO_TILE)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Check if this block is disallowed from acceleration
        BlockState blockState = level.getBlockState(pos);
        if (blockState.is(Constants.Tags.DISALLOW_ACCELERATION)) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TEMPORAL_DISALLOWED)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Check if GAG, TIAB, or JDT is already accelerating this block
        if (isAcceleratedByGAG(pos, level) || isAcceleratedByTIAB(pos, level) || isAcceleratedByJDT(pos, level)) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TEMPORAL_GAG_ACTIVE)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        IAnima network = AnimusRitualHelper.getNetworkForBoundItem(player, stack);
        if (network == null) {
            return false;
        }

        // Get or create acceleration state
        AccelerationState state = acceleratedBlocks.get(pos);
        int newLevel;
        int evCost;

        if (state == null) {
            // First activation
            newLevel = 1;
            evCost = BASE_EV_COST;
        } else if (state.level >= MAX_LEVEL) {
            // Already at max level, just refresh timer
            newLevel = MAX_LEVEL;
            evCost = REFRESH_COST;
        } else {
            // Increase level
            newLevel = state.level + 1;
            evCost = BASE_EV_COST * (1 << (newLevel - 1)); // 1000 * 2^(level-1)
        }

        // Check if player has enough EV
        int currentEV = network.getCurrentEV();
        if (currentEV < evCost) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.TEMPORAL_NO_EV, evCost)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Consume EV
        network.syphon(AnimaTicket.create(evCost));

        // Update or create acceleration state
        long expiryTime = level.getGameTime() + DURATION_TICKS;
        acceleratedBlocks.put(pos.immutable(), new AccelerationState(newLevel, expiryTime, level.dimension()));

        // Calculate speed multiplier
        int speedMultiplier = 1 << newLevel; // 2^level

        // Send feedback to player
        player.displayClientMessage(
                Component.translatable(
                        Constants.Localizations.Text.TEMPORAL_ACTIVATED,
                        speedMultiplier,
                        DURATION_TICKS / 20
                ).withStyle(ChatFormatting.GOLD),
                true
        );

        // Return false to skip EV cost from sigil_type (we handle it ourselves)
        return false;
    }

    /**
     * Tick all accelerated block entities.
     * Called from event handler.
     */
    public static void tickAcceleratedBlocks(ServerLevel level) {
        long currentTime = level.getGameTime();
        Iterator<Map.Entry<BlockPos, AccelerationState>> iterator = acceleratedBlocks.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<BlockPos, AccelerationState> entry = iterator.next();
            BlockPos pos = entry.getKey();
            AccelerationState state = entry.getValue();

            // Only process blocks in this dimension
            if (!state.dimension.equals(level.dimension())) {
                continue;
            }

            // Check if expired
            if (currentTime >= state.expiryTime) {
                iterator.remove();
                continue;
            }

            // Get the block entity
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                iterator.remove();
                continue;
            }

            // Get the block state
            BlockState blockState = level.getBlockState(pos);
            Block block = blockState.getBlock();

            // Calculate how many extra ticks to perform
            int speedMultiplier = 1 << state.level; // 2^level
            int extraTicks = speedMultiplier - 1; // Subtract 1 since it will tick normally once

            // Get the block entity ticker from the block (only EntityBlock has getTicker)
            BlockEntityTicker<?> ticker = null;
            if (block instanceof EntityBlock entityBlock) {
                ticker = entityBlock.getTicker(level, blockState, blockEntity.getType());
            }

            // Perform extra ticks by calling the ticker directly
            // Note: Some block entities don't have server-side tickers (only client tickers)
            // or use other mechanisms (random ticks, neighbor updates, etc.)
            // We keep the entry regardless so the level can increment and visual feedback works
            if (ticker != null) {
                for (int i = 0; i < extraTicks; i++) {
                    @SuppressWarnings("unchecked")
                    BlockEntityTicker<BlockEntity> safeTicker = (BlockEntityTicker<BlockEntity>) ticker;
                    safeTicker.tick(level, pos, blockState, blockEntity);
                }
            }
            // If no ticker, we still keep the entry - it will expire naturally
        }
    }

    /**
     * Get acceleration state for rendering overlay.
     */
    public static Map<BlockPos, AccelerationState> getAcceleratedBlocks() {
        return Collections.unmodifiableMap(acceleratedBlocks);
    }

    /**
     * Sync accelerated blocks to all players in the dimension.
     * Called periodically from event handler.
     */
    public static void syncToClients(ServerLevel level) {
        if (acceleratedBlocks.isEmpty()) {
            // Send empty packet to clear client data
            AcceleratedBlocksSyncPayload payload = new AcceleratedBlocksSyncPayload(new HashMap<>());
            for (ServerPlayer player : level.players()) {
                AnimusPayloads.sendToPlayer(player, payload);
            }
            return;
        }

        // Convert internal state to payload data, filtering by dimension
        Map<BlockPos, AcceleratedBlocksSyncPayload.AccelerationEntry> payloadData = new HashMap<>();
        for (Map.Entry<BlockPos, AccelerationState> entry : acceleratedBlocks.entrySet()) {
            AccelerationState state = entry.getValue();
            if (state.dimension.equals(level.dimension())) {
                payloadData.put(entry.getKey(), new AcceleratedBlocksSyncPayload.AccelerationEntry(
                        state.level,
                        state.expiryTime,
                        state.dimension
                ));
            }
        }

        // Send to all players in this dimension
        AcceleratedBlocksSyncPayload payload = new AcceleratedBlocksSyncPayload(payloadData);
        for (ServerPlayer player : level.players()) {
            AnimusPayloads.sendToPlayer(player, payload);
        }
    }

    /**
     * Check if GAG (Gadgets Against Grind) is accelerating this block.
     * GAG spawns a TimeAcceleratorEntity at the block position.
     */
    private static boolean isAcceleratedByGAG(BlockPos pos, Level level) {
        return hasAcceleratorEntity(pos, level, "ky.someone.mods.gag.entity.TimeAcceleratorEntity");
    }

    /**
     * Check if Time in a Bottle is accelerating this block.
     * TIAB spawns a TimeAcceleratorEntity at the block position.
     */
    private static boolean isAcceleratedByTIAB(BlockPos pos, Level level) {
        return hasAcceleratorEntity(pos, level, "org.mangorage.tiab.common.entities.TimeAcceleratorEntity")
                || hasAcceleratorEntity(pos, level, "com.haoict.tiab.common.entities.TimeAcceleratorEntity");
    }

    /**
     * Check if JustDireThings is accelerating this block.
     * JDT spawns a TimeWandEntity at the block position.
     */
    private static boolean isAcceleratedByJDT(BlockPos pos, Level level) {
        return hasAcceleratorEntity(pos, level, "com.direwolf20.justdirethings.common.entities.TimeWandEntity");
    }

    private static boolean hasAcceleratorEntity(BlockPos pos, Level level, String className) {
        try {
            Class.forName(className);
            List<? extends Entity> entities = level.getEntities(
                    (Entity) null,
                    new AABB(pos),
                    entity -> entity != null && entity.getClass().getName().equals(className)
            );
            return !entities.isEmpty();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Track acceleration state for a block entity.
     */
    public static class AccelerationState {
        public final int level;
        public final long expiryTime;
        public final ResourceKey<Level> dimension;

        public AccelerationState(int level, long expiryTime, ResourceKey<Level> dimension) {
            this.level = level;
            this.expiryTime = expiryTime;
            this.dimension = dimension;
        }

        public int getSpeedMultiplier() {
            return 1 << level; // 2^level
        }

        public long getRemainingTicks(long currentTime) {
            return Math.max(0, expiryTime - currentTime);
        }
    }
}
