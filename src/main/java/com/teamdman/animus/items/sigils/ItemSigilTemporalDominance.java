package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.items.sigils.AnimusSigilBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sigil of Temporal Dominance
 * Accelerates tick rate of targeted block entities using LP
 *
 * Progression:
 * - Level 1: 2x speed, 1000 LP, 30 seconds
 * - Level 2: 4x speed, 2000 LP, 30 seconds
 * - Level 3: 8x speed, 4000 LP, 30 seconds
 * - Level 4: 16x speed, 8000 LP, 30 seconds
 * - Level 5: 32x speed, 16000 LP, 30 seconds
 * - Level 5+: 32x speed, 1000 LP to refresh timer
 *
 * When timer expires, all calculations reset
 */
public class ItemSigilTemporalDominance extends AnimusSigilBase implements IBindable {
    // Maximum acceleration level (32x speed = 2^5)
    private static final int MAX_LEVEL = 5;
    // Base LP cost for first activation
    private static final int BASE_LP_COST = 1000;
    // Duration in ticks (30 seconds = 600 ticks)
    private static final int DURATION_TICKS = 600;
    // Refresh cost when at max level
    private static final int REFRESH_COST = 1000;

    // Track acceleration state for each block entity
    private static final Map<BlockPos, AccelerationState> acceleratedBlocks = new ConcurrentHashMap<>();

    // GAG compatibility - cache the reflection check
    private static Boolean gagLoaded = null;
    private static java.lang.reflect.Method gagCheckMethod = null;

    /**
     * Check if GAG (Gadgets Against Grind) is accelerating this block
     * Uses reflection to avoid hard dependency
     */
    private static boolean isAcceleratedByGAG(BlockPos pos, Level level) {
        // One-time check if GAG is loaded
        if (gagLoaded == null) {
            try {
                Class<?> temporalPouchClass = Class.forName("ky.someone.mods.gag.item.TemporalPouchItem");
                // Try to find a static method or field that tracks accelerated blocks
                try {
                    gagCheckMethod = temporalPouchClass.getMethod("isAccelerated", BlockPos.class, Level.class);
                    gagLoaded = true;
                } catch (NoSuchMethodException e) {
                    // Try to access the static map directly
                    try {
                        java.lang.reflect.Field acceleratedField = temporalPouchClass.getDeclaredField("acceleratedBlocks");
                        acceleratedField.setAccessible(true);
                        gagLoaded = true;
                    } catch (NoSuchFieldException ex) {
                        gagLoaded = false;
                    }
                }
            } catch (ClassNotFoundException e) {
                // GAG not loaded
                gagLoaded = false;
            }
        }

        // If GAG isn't loaded, return false
        if (!gagLoaded) {
            return false;
        }

        // Try to check if this block is accelerated by GAG
        try {
            if (gagCheckMethod != null) {
                return (Boolean) gagCheckMethod.invoke(null, pos, level);
            } else {
                // Try to access the map directly
                Class<?> temporalPouchClass = Class.forName("ky.someone.mods.gag.item.TemporalPouchItem");
                java.lang.reflect.Field acceleratedField = temporalPouchClass.getDeclaredField("acceleratedBlocks");
                acceleratedField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<?, ?> acceleratedMap = (Map<?, ?>) acceleratedField.get(null);
                return acceleratedMap.containsKey(pos);
            }
        } catch (Exception e) {
            // If anything fails, just return false and proceed
            return false;
        }
    }

    /**
     * Check if Time in a Bottle is accelerating this block
     * TIAB spawns a TimeAcceleratorEntity at the block position
     */
    private static boolean isAcceleratedByTIAB(BlockPos pos, Level level) {
        try {
            Class<?> timeAcceleratorClass = Class.forName("com.haoict.tiab.common.entities.TimeAcceleratorEntity");
            // Check for any TimeAcceleratorEntity at this position
            java.util.List<? extends net.minecraft.world.entity.Entity> entities = level.getEntities(
                (net.minecraft.world.entity.Entity) null,
                new net.minecraft.world.phys.AABB(pos),
                entity -> entity != null && entity.getClass().getName().equals("com.haoict.tiab.common.entities.TimeAcceleratorEntity")
            );
            return !entities.isEmpty();
        } catch (ClassNotFoundException e) {
            // TIAB not loaded
            return false;
        }
    }

    /**
     * Check if JustDireThings is accelerating this block
     * JDT spawns a TimeWandEntity at the block position
     */
    private static boolean isAcceleratedByJDT(BlockPos pos, Level level) {
        try {
            Class<?> timeWandEntityClass = Class.forName("com.direwolf20.justdirethings.common.entities.TimeWandEntity");
            // Check for any TimeWandEntity at this position
            java.util.List<? extends net.minecraft.world.entity.Entity> entities = level.getEntities(
                (net.minecraft.world.entity.Entity) null,
                new net.minecraft.world.phys.AABB(pos),
                entity -> entity != null && entity.getClass().getName().equals("com.direwolf20.justdirethings.common.entities.TimeWandEntity")
            );
            return !entities.isEmpty();
        } catch (ClassNotFoundException e) {
            // JDT not loaded
            return false;
        }
    }

    public ItemSigilTemporalDominance() {
        super(Constants.Sigils.TEMPORAL_DOMINANCE, 0); // LP cost handled in activation
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (level.isClientSide || player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        // Check if there's a block entity at this position
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.TEMPORAL_NO_TILE)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // Check if this block is disallowed from acceleration
        net.minecraft.world.level.block.state.BlockState blockState = level.getBlockState(pos);
        if (blockState.is(Constants.Tags.DISALLOW_ACCELERATION)) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.TEMPORAL_DISALLOWED)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // Check if GAG, TIAB, or JDT is already accelerating this block
        if (isAcceleratedByGAG(pos, level) || isAcceleratedByTIAB(pos, level) || isAcceleratedByJDT(pos, level)) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.TEMPORAL_GAG_ACTIVE)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // Get the player's soul network
        SoulNetwork network = NetworkHelper.getSoulNetwork((ServerPlayer) player);
        if (network == null) {
            return InteractionResult.FAIL;
        }

        // Get or create acceleration state
        AccelerationState state = acceleratedBlocks.get(pos);
        int newLevel;
        int lpCost;

        if (state == null) {
            // First activation
            newLevel = 1;
            lpCost = BASE_LP_COST;
        } else if (state.level >= MAX_LEVEL) {
            // Already at max level, just refresh timer
            newLevel = MAX_LEVEL;
            lpCost = REFRESH_COST;
        } else {
            // Increase level
            newLevel = state.level + 1;
            lpCost = BASE_LP_COST * (1 << (newLevel - 1)); // 1000 * 2^(level-1)
        }

        // Check if player has enough LP
        int currentEssence = network.getCurrentEssence();
        if (currentEssence < lpCost) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.TEMPORAL_NO_LP, lpCost)
                    .withStyle(ChatFormatting.RED),
                true
            );
            network.causeNausea();
            return InteractionResult.FAIL;
        }

        // Consume LP
        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_TEMPORAL_DOMINANCE),
            lpCost
        ), false);

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

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.TEMPORAL_DOMINANCE_1)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.TEMPORAL_DOMINANCE_2)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.TEMPORAL_DOMINANCE_3)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.TEMPORAL_DOMINANCE_4)
            .withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    /**
     * Tick all accelerated block entities
     * Called from event handler
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
            net.minecraft.world.level.block.state.BlockState blockState = level.getBlockState(pos);
            net.minecraft.world.level.block.Block block = blockState.getBlock();

            // Calculate how many extra ticks to perform
            int speedMultiplier = 1 << state.level; // 2^level
            int extraTicks = speedMultiplier - 1; // Subtract 1 since it will tick normally once

            // Get the block entity ticker from the block (only EntityBlock has getTicker)
            // This is how GAG's TimeAcceleratorEntity does it
            net.minecraft.world.level.block.entity.BlockEntityTicker<?> ticker = null;
            if (block instanceof net.minecraft.world.level.block.EntityBlock entityBlock) {
                ticker = entityBlock.getTicker(level, blockState, blockEntity.getType());
            }

            // Perform extra ticks by calling the ticker directly
            if (ticker != null) {
                for (int i = 0; i < extraTicks; i++) {
                    // Cast is safe because we got the ticker from the same block entity type
                    @SuppressWarnings("unchecked")
                    net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity> safeTicker =
                        (net.minecraft.world.level.block.entity.BlockEntityTicker<BlockEntity>) ticker;
                    safeTicker.tick(level, pos, blockState, blockEntity);
                }
            } else {
                // No ticker available, remove this block from acceleration
                iterator.remove();
            }
        }
    }

    /**
     * Get acceleration state for rendering overlay
     */
    public static Map<BlockPos, AccelerationState> getAcceleratedBlocks() {
        return Collections.unmodifiableMap(acceleratedBlocks);
    }

    /**
     * Track acceleration state for a block entity
     */
    public static class AccelerationState {
        public final int level;
        public final long expiryTime;
        public final net.minecraft.resources.ResourceKey<Level> dimension;

        public AccelerationState(int level, long expiryTime, net.minecraft.resources.ResourceKey<Level> dimension) {
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
