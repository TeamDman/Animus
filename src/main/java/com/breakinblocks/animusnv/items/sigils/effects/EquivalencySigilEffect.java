package com.breakinblocks.animusnv.items.sigils.effects;

import com.mojang.serialization.MapCodec;
import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import com.breakinblocks.neovitae.api.sigil.ISigilEffect;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sigil of Equivalency - Equal-trade style sigil that swaps blocks in a radius using EV.
 * <p>
 * - Sneak + right-click block: Add to selection list (up to 20)
 * - Sneak + right-click air: Clear selection
 * - Right-click block: Replace all matching blocks in radius with random selection
 * - Scroll while sneaking: Adjust radius (handled via event handler)
 */
public record EquivalencySigilEffect() implements ISigilEffect {
    public static final MapCodec<EquivalencySigilEffect> CODEC = MapCodec.unit(EquivalencySigilEffect::new);

    private static final int MAX_SELECTED_BLOCKS = 20;
    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 32;

    // Track ongoing replacement operations
    private static final Map<UUID, ReplacementOperation> activeOperations = new ConcurrentHashMap<>();

    @Override
    public MapCodec<? extends ISigilEffect> codec() {
        return CODEC;
    }

    @Override
    public void onPlayerLogout(UUID playerId, MinecraftServer server) {
        activeOperations.remove(playerId);
    }

    @Override
    public boolean useOnAir(Level level, Player player, ItemStack stack) {
        // Sneak + right-click air to clear selection
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                clearSelectedBlocks(stack);
                player.displayClientMessage(
                        Component.translatable(Constants.Localizations.Text.EQUIVALENCY_CLEARED)
                                .withStyle(ChatFormatting.GOLD),
                        true
                );
            }
            return false; // Don't consume EV for clearing
        }

        return false;
    }

    @Override
    public boolean useOnBlock(Level level, Player player, ItemStack stack, BlockPos pos, Direction side, Vec3 hitVec) {
        BlockState targetState = level.getBlockState(pos);
        if (targetState.isAir()) {
            return false;
        }

        if (level.isClientSide) {
            return true; // Consume on client to prevent further processing
        }

        // Sneak + right-click to add block to selection
        if (player.isShiftKeyDown()) {
            return handleBlockSelection(level, player, stack, targetState);
        }

        // Right-click to perform replacement
        return handleBlockReplacement((ServerLevel) level, (ServerPlayer) player, stack, pos, targetState, side);
    }

    private boolean handleBlockSelection(Level level, Player player, ItemStack stack, BlockState targetState) {
        List<Block> selectedBlocks = getSelectedBlocks(stack);
        Block targetBlock = targetState.getBlock();

        // Check if already selected
        if (selectedBlocks.contains(targetBlock)) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.EQUIVALENCY_ALREADY_SELECTED)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Check max selection
        if (selectedBlocks.size() >= MAX_SELECTED_BLOCKS) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.EQUIVALENCY_MAX_SELECTED, MAX_SELECTED_BLOCKS)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Add to selection
        selectedBlocks.add(targetBlock);
        setSelectedBlocks(stack, selectedBlocks);

        player.displayClientMessage(
                Component.translatable(
                        Constants.Localizations.Text.EQUIVALENCY_ADDED,
                        targetBlock.getName(),
                        selectedBlocks.size(),
                        MAX_SELECTED_BLOCKS
                ).withStyle(ChatFormatting.GREEN),
                true
        );

        return false; // Don't consume EV for selection
    }

    private boolean handleBlockReplacement(ServerLevel level, ServerPlayer player, ItemStack stack,
                                           BlockPos centerPos, BlockState targetState, Direction clickedFace) {
        // Check if we have selected blocks
        List<Block> selectedBlocks = getSelectedBlocks(stack);
        if (selectedBlocks.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.EQUIVALENCY_NO_SELECTION)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Filter selected blocks that are in inventory (skip check for creative mode)
        List<Block> availableBlocks = new ArrayList<>();
        if (player.isCreative()) {
            availableBlocks.addAll(selectedBlocks);
        } else {
            for (Block block : selectedBlocks) {
                if (hasBlockInInventory(player, block)) {
                    availableBlocks.add(block);
                }
            }
        }

        if (availableBlocks.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.EQUIVALENCY_NO_BLOCKS)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        IAnima network = AnimusRitualHelper.getNetworkForBoundItem(player, stack);
        if (network == null) {
            return false;
        }

        // Find all matching blocks in radius
        int radius = getRadius(stack);
        List<BlockPos> matchingBlocks = findMatchingBlocksInRadius(level, centerPos, targetState.getBlock(), radius, clickedFace);

        if (matchingBlocks.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.EQUIVALENCY_NO_MATCHES)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Filter out blocks that would be replaced with the same type
        List<BlockPos> blocksToReplace = new ArrayList<>();
        for (BlockPos blockPos : matchingBlocks) {
            boolean canReplace = false;
            for (Block availableBlock : availableBlocks) {
                if (availableBlock != targetState.getBlock()) {
                    canReplace = true;
                    break;
                }
            }
            if (canReplace) {
                blocksToReplace.add(blockPos);
            }
        }

        if (blocksToReplace.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("text.component.animusnv.equivalency.same_block_warning")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Calculate total EV cost based on blocks that will actually be replaced
        int lpPerBlock = AnimusConfig.sigils.sigilEquivalencyEVCost.get();
        int totalEV = blocksToReplace.size() * lpPerBlock;

        if (network.getCurrentEV() < totalEV) {
            player.displayClientMessage(
                    Component.translatable(Constants.Localizations.Text.EQUIVALENCY_NO_EV, totalEV)
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return false;
        }

        // Consume EV
        network.syphon(AnimaTicket.create(totalEV));

        // Create replacement operation
        ReplacementOperation operation = new ReplacementOperation(
                level,
                player.getUUID(),
                blocksToReplace,
                availableBlocks,
                targetState.getBlock(),
                centerPos
        );
        activeOperations.put(player.getUUID(), operation);

        // Play sound once at start (30% volume)
        level.playSound(null, centerPos, SoundEvents.PORTAL_TRAVEL,
                SoundSource.BLOCKS, 0.3f, 1.5f);

        player.displayClientMessage(
                Component.translatable(
                        Constants.Localizations.Text.EQUIVALENCY_STARTED,
                        blocksToReplace.size()
                ).withStyle(ChatFormatting.GOLD),
                true
        );

        // Return false to skip EV cost from sigil_type (we handle it ourselves)
        return false;
    }

    /**
     * Find all connected blocks matching the target using flood-fill (BFS).
     * Only finds blocks on the same plane as the clicked face.
     */
    private List<BlockPos> findMatchingBlocksInRadius(ServerLevel level, BlockPos center, Block targetBlock, int radius, Direction clickedFace) {
        List<BlockPos> matches = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        // Maximum blocks for a plane (square area)
        int maxBlocks = (radius * 2 + 1) * (radius * 2 + 1);

        // Start flood-fill from center
        queue.add(center);
        visited.add(center);

        // Determine which neighbors to use based on clicked face
        BlockPos[] neighbors;
        if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            // Horizontal plane - only check cardinal directions on XZ plane
            neighbors = new BlockPos[]{
                    new BlockPos(1, 0, 0),
                    new BlockPos(-1, 0, 0),
                    new BlockPos(0, 0, 1),
                    new BlockPos(0, 0, -1)
            };
        } else if (clickedFace == Direction.NORTH || clickedFace == Direction.SOUTH) {
            // North/South vertical plane - check on XY plane
            neighbors = new BlockPos[]{
                    new BlockPos(1, 0, 0),
                    new BlockPos(-1, 0, 0),
                    new BlockPos(0, 1, 0),
                    new BlockPos(0, -1, 0)
            };
        } else {
            // East/West vertical plane - check on ZY plane
            neighbors = new BlockPos[]{
                    new BlockPos(0, 0, 1),
                    new BlockPos(0, 0, -1),
                    new BlockPos(0, 1, 0),
                    new BlockPos(0, -1, 0)
            };
        }

        while (!queue.isEmpty() && matches.size() < maxBlocks) {
            BlockPos current = queue.poll();

            // Check if on the same plane and within radius
            if (!isOnSamePlaneAndInRadius(current, center, radius, clickedFace)) {
                continue;
            }

            // Check if this block matches
            BlockState state = level.getBlockState(current);
            if (state.getBlock() == targetBlock) {
                matches.add(current.immutable());

                // Add neighbors to queue
                for (BlockPos offset : neighbors) {
                    BlockPos neighbor = current.offset(offset.getX(), offset.getY(), offset.getZ());
                    if (!visited.contains(neighbor) && isOnSamePlaneAndInRadius(neighbor, center, radius, clickedFace)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return matches;
    }

    /**
     * Check if a position is on the same plane as center and within radius.
     */
    private boolean isOnSamePlaneAndInRadius(BlockPos pos, BlockPos center, int radius, Direction clickedFace) {
        int dx = Math.abs(pos.getX() - center.getX());
        int dy = Math.abs(pos.getY() - center.getY());
        int dz = Math.abs(pos.getZ() - center.getZ());

        if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            return pos.getY() == center.getY() && Math.max(dx, dz) <= radius;
        } else if (clickedFace == Direction.NORTH || clickedFace == Direction.SOUTH) {
            return pos.getZ() == center.getZ() && Math.max(dx, dy) <= radius;
        } else {
            return pos.getX() == center.getX() && Math.max(dy, dz) <= radius;
        }
    }

    /**
     * Check if player has the block item in inventory.
     */
    private boolean hasBlockInInventory(Player player, Block block) {
        ItemStack blockItem = new ItemStack(block.asItem());
        if (blockItem.isEmpty()) {
            return false;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, blockItem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process active replacement operations.
     * Called from event handler.
     */
    public static void tickReplacements(ServerLevel level) {
        if (activeOperations.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, ReplacementOperation>> iterator = activeOperations.entrySet().iterator();
        int blocksPerTick = AnimusConfig.sigils.sigilEquivalencyBlocksPerTick.get();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ReplacementOperation> entry = iterator.next();
            ReplacementOperation operation = entry.getValue();

            if (!operation.level.equals(level)) {
                continue;
            }

            boolean finished = operation.processBlocks(blocksPerTick);
            if (finished) {
                iterator.remove();
            }
        }
    }

    // Data Component Methods for selected blocks
    private List<Block> getSelectedBlocks(ItemStack stack) {
        List<Block> blocks = new ArrayList<>();
        String selectedStr = stack.get(AnimusDataComponents.EQUIVALENCY_SELECTED_BLOCKS.get());
        if (selectedStr == null || selectedStr.isEmpty()) {
            return blocks;
        }

        String[] blockIds = selectedStr.split(",");
        for (String blockId : blockIds) {
            if (blockId.isEmpty()) continue;
            ResourceLocation rl = ResourceLocation.tryParse(blockId);
            if (rl != null) {
                Block block = BuiltInRegistries.BLOCK.getOptional(rl).orElse(null);
                if (block != null && block != Blocks.AIR) {
                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    private void setSelectedBlocks(ItemStack stack, List<Block> blocks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id != null) {
                if (i > 0) sb.append(",");
                sb.append(id.toString());
            }
        }
        stack.set(AnimusDataComponents.EQUIVALENCY_SELECTED_BLOCKS.get(), sb.toString());
    }

    private void clearSelectedBlocks(ItemStack stack) {
        stack.remove(AnimusDataComponents.EQUIVALENCY_SELECTED_BLOCKS.get());
    }

    // Radius data component methods
    public static int getRadius(ItemStack stack) {
        Integer radiusVal = stack.get(AnimusDataComponents.EQUIVALENCY_RADIUS.get());
        int radius;
        if (radiusVal == null) {
            radius = AnimusConfig.sigils.sigilEquivalencyRadius.get();
        } else {
            radius = radiusVal;
        }
        return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
    }

    /**
     * Set the radius for an Equivalency sigil.
     * Public for use by scroll event handler.
     */
    public static void setRadius(ItemStack stack, int radius) {
        radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
        stack.set(AnimusDataComponents.EQUIVALENCY_RADIUS.get(), radius);
    }

    /**
     * Get the maximum radius.
     */
    public static int getMaxRadius() {
        return MAX_RADIUS;
    }

    /**
     * Get the minimum radius.
     */
    public static int getMinRadius() {
        return MIN_RADIUS;
    }

    /**
     * Tracks an ongoing block replacement operation.
     */
    private static class ReplacementOperation {
        private final ServerLevel level;
        private final UUID playerUUID;
        private final List<BlockPos> positions;
        private final List<Block> replacementBlocks;
        private final Block originalBlock;
        private final BlockPos centerPos;
        private int currentIndex = 0;
        private final Random random = new Random();

        public ReplacementOperation(ServerLevel level, UUID playerUUID, List<BlockPos> positions,
                                    List<Block> replacementBlocks, Block originalBlock, BlockPos centerPos) {
            this.level = level;
            this.playerUUID = playerUUID;
            this.positions = positions;
            this.replacementBlocks = replacementBlocks;
            this.originalBlock = originalBlock;
            this.centerPos = centerPos;
        }

        /**
         * Process up to maxBlocks replacements.
         * Returns true if operation is finished.
         */
        public boolean processBlocks(int maxBlocks) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUUID);
            if (player == null) {
                return true; // Player disconnected, cancel operation
            }

            int processed = 0;
            while (currentIndex < positions.size() && processed < maxBlocks) {
                BlockPos pos = positions.get(currentIndex);
                replaceBlock(player, pos);
                currentIndex++;
                processed++;
            }

            return currentIndex >= positions.size();
        }

        private void replaceBlock(ServerPlayer player, BlockPos pos) {
            BlockState currentState = level.getBlockState(pos);

            // Verify block hasn't changed
            if (currentState.getBlock() != originalBlock) {
                return;
            }

            // Select random replacement block
            Block replacementBlock = replacementBlocks.get(random.nextInt(replacementBlocks.size()));

            // Skip if somehow the same
            if (currentState.getBlock() == replacementBlock) {
                return;
            }

            // Get drops with silk touch
            ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
            if (level.registryAccess().lookup(Registries.ENCHANTMENT).isPresent()) {
                var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var silkTouch = enchantmentRegistry.get(Enchantments.SILK_TOUCH);
                if (silkTouch.isPresent()) {
                    tool.enchant(silkTouch.get(), 1);
                }
            }

            LootParams.Builder lootBuilder = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, tool)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                    .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));

            List<ItemStack> drops = currentState.getDrops(lootBuilder);

            // Give drops to player (skip in creative mode)
            if (!player.isCreative()) {
                for (ItemStack drop : drops) {
                    if (!player.getInventory().add(drop)) {
                        player.drop(drop, false);
                    }
                }
            }

            // Consume block from inventory (skip in creative mode)
            if (!player.isCreative()) {
                if (!consumeBlockFromInventory(player, replacementBlock)) {
                    return; // No blocks left, skip
                }
            }

            // Place new block
            BlockState newState = replacementBlock.defaultBlockState();
            level.setBlock(pos, newState, 3);

            // Spawn particles on ~20% of blocks for visual feedback without spam
            if (random.nextFloat() < 0.2f) {
                level.sendParticles(
                        ParticleTypes.WITCH,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        3, 0.3, 0.3, 0.3, 0.02
                );
            }
        }

        private boolean consumeBlockFromInventory(ServerPlayer player, Block block) {
            ItemStack blockItem = new ItemStack(block.asItem());
            if (blockItem.isEmpty()) {
                return false;
            }

            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (ItemStack.isSameItemSameComponents(stack, blockItem)) {
                    stack.shrink(1);
                    return true;
                }
            }
            return false;
        }
    }
}
