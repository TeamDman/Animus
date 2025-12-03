package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import com.teamdman.animus.client.InputClientHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import wayoftime.bloodmagic.common.item.IBindable;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sigil of Equivalency
 * Equal-trade style sigil that swaps blocks in a radius using LP
 *
 * - Sneak + right-click block: Add to selection list (up to 20)
 * - Sneak + right-click air: Clear selection
 * - Right-click block: Replace all matching blocks in radius with random selection
 */
public class ItemSigilEquivalency extends AnimusSigilBase implements IBindable {
    private static final String NBT_SELECTED_BLOCKS = "SelectedBlocks";
    private static final String NBT_RADIUS = "Radius";
    private static final int MAX_SELECTED_BLOCKS = 20;
    private static final int MIN_RADIUS = 1;
    private static final int MAX_RADIUS = 32;

    // Track ongoing replacement operations
    private static final Map<UUID, ReplacementOperation> activeOperations = new ConcurrentHashMap<>();

    public ItemSigilEquivalency() {
        super(Constants.Sigils.EQUIVALENCY, 0); // LP cost per block
    }

    public boolean onScroll(Player player, ItemStack stack, double scrollDelta, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return false;
        }

        // Adjust radius
        int currentRadius = getRadius(stack);
        int newRadius = currentRadius + (scrollDelta > 0 ? 1 : -1);
        newRadius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, newRadius));

        if (newRadius != currentRadius) {
            setRadius(stack, newRadius);
            int areaDimension = (newRadius * 2) + 1;

            if (player.level().isClientSide) {
                // Send packet to server to sync radius
                com.teamdman.animus.network.AnimusNetwork.CHANNEL.sendToServer(
                    new com.teamdman.animus.network.SigilRadiusPacket(hand, newRadius)
                );

                player.displayClientMessage(
                    Component.translatable("text.component.animus.equivalency.radius_changed", newRadius, areaDimension, areaDimension)
                        .withStyle(ChatFormatting.GOLD),
                    true
                );
            }
        }

        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

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
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState targetState = level.getBlockState(pos);
        if (targetState.isAir()) {
            return InteractionResult.FAIL;
        }

        // On client side, consume the interaction to prevent use() from being called
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        // Sneak + right-click to add block to selection
        if (player.isShiftKeyDown()) {
            return handleBlockSelection(level, player, stack, targetState);
        }

        // Right-click to perform replacement
        return handleBlockReplacement((ServerLevel) level, (ServerPlayer) player, stack, pos, targetState, context.getClickedFace());
    }

    private InteractionResult handleBlockSelection(Level level, Player player, ItemStack stack, BlockState targetState) {
        List<Block> selectedBlocks = getSelectedBlocks(stack);
        Block targetBlock = targetState.getBlock();

        // Check if already selected
        if (selectedBlocks.contains(targetBlock)) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.EQUIVALENCY_ALREADY_SELECTED)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // Check max selection
        if (selectedBlocks.size() >= MAX_SELECTED_BLOCKS) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.EQUIVALENCY_MAX_SELECTED, MAX_SELECTED_BLOCKS)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // Add to selection (no inventory check - that happens during replacement)
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

        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleBlockReplacement(ServerLevel level, ServerPlayer player, ItemStack stack,
                                                      BlockPos centerPos, BlockState targetState, net.minecraft.core.Direction clickedFace) {
        // Check if we have selected blocks
        List<Block> selectedBlocks = getSelectedBlocks(stack);
        if (selectedBlocks.isEmpty()) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.EQUIVALENCY_NO_SELECTION)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // Filter selected blocks that are in inventory (skip check for creative mode)
        List<Block> availableBlocks = new ArrayList<>();
        if (player.isCreative()) {
            // In creative mode, all selected blocks are available
            availableBlocks.addAll(selectedBlocks);
        } else {
            // In survival/adventure, only use blocks from inventory
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
            return InteractionResult.FAIL;
        }

        // Get the player's soul network
        SoulNetwork network = NetworkHelper.getSoulNetwork(player);
        if (network == null) {
            return InteractionResult.FAIL;
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
            return InteractionResult.FAIL;
        }

        // Filter out blocks that would be replaced with the same type
        List<BlockPos> blocksToReplace = new ArrayList<>();
        for (BlockPos pos : matchingBlocks) {
            // Check if any available block is different from current
            boolean canReplace = false;
            for (Block availableBlock : availableBlocks) {
                if (availableBlock != targetState.getBlock()) {
                    canReplace = true;
                    break;
                }
            }
            if (canReplace) {
                blocksToReplace.add(pos);
            }
        }

        if (blocksToReplace.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("text.component.animus.equivalency.same_block_warning")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // Calculate total LP cost based on blocks that will actually be replaced
        int lpPerBlock = AnimusConfig.sigils.sigilEquivalencyLPCost.get();
        int totalLP = blocksToReplace.size() * lpPerBlock;

        if (network.getCurrentEssence() < totalLP) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.EQUIVALENCY_NO_LP, totalLP)
                    .withStyle(ChatFormatting.RED),
                true
            );
            network.causeNausea();
            return InteractionResult.FAIL;
        }

        // Consume LP
        network.syphon(new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_EQUIVALENCY),
            totalLP
        ), false);

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
        level.playSound(null, centerPos, net.minecraft.sounds.SoundEvents.PORTAL_TRAVEL,
            net.minecraft.sounds.SoundSource.BLOCKS, 0.3f, 1.5f);

        player.displayClientMessage(
            Component.translatable(
                Constants.Localizations.Text.EQUIVALENCY_STARTED,
                blocksToReplace.size()
            ).withStyle(ChatFormatting.GOLD),
            true
        );

        return InteractionResult.SUCCESS;
    }

    /**
     * Find all connected blocks matching the target using flood-fill (BFS)
     * Only finds blocks on the same plane as the clicked face
     */
    private List<BlockPos> findMatchingBlocksInRadius(ServerLevel level, BlockPos center, Block targetBlock, int radius, net.minecraft.core.Direction clickedFace) {
        List<BlockPos> matches = new ArrayList<>();
        Set<BlockPos> visited = new java.util.HashSet<>();
        Queue<BlockPos> queue = new java.util.LinkedList<>();

        // Maximum blocks for a plane (square area)
        int maxBlocks = (radius * 2 + 1) * (radius * 2 + 1);

        // Start flood-fill from center
        queue.add(center);
        visited.add(center);

        // Determine which neighbors to use based on clicked face
        // For horizontal faces (UP/DOWN), only check horizontal neighbors
        // For vertical faces, only check neighbors on that vertical plane
        BlockPos[] neighbors;
        if (clickedFace == net.minecraft.core.Direction.UP || clickedFace == net.minecraft.core.Direction.DOWN) {
            // Horizontal plane - only check cardinal directions on XZ plane
            neighbors = new BlockPos[] {
                new BlockPos(1, 0, 0),   // East
                new BlockPos(-1, 0, 0),  // West
                new BlockPos(0, 0, 1),   // South
                new BlockPos(0, 0, -1)   // North
            };
        } else if (clickedFace == net.minecraft.core.Direction.NORTH || clickedFace == net.minecraft.core.Direction.SOUTH) {
            // North/South vertical plane - check on XY plane
            neighbors = new BlockPos[] {
                new BlockPos(1, 0, 0),   // East
                new BlockPos(-1, 0, 0),  // West
                new BlockPos(0, 1, 0),   // Up
                new BlockPos(0, -1, 0)   // Down
            };
        } else {
            // East/West vertical plane - check on ZY plane
            neighbors = new BlockPos[] {
                new BlockPos(0, 0, 1),   // South
                new BlockPos(0, 0, -1),  // North
                new BlockPos(0, 1, 0),   // Up
                new BlockPos(0, -1, 0)   // Down
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

                    // Only process if not visited and on same plane within radius
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
     * Check if a position is on the same plane as center and within radius
     */
    private boolean isOnSamePlaneAndInRadius(BlockPos pos, BlockPos center, int radius, net.minecraft.core.Direction clickedFace) {
        int dx = Math.abs(pos.getX() - center.getX());
        int dy = Math.abs(pos.getY() - center.getY());
        int dz = Math.abs(pos.getZ() - center.getZ());

        // Check plane constraint and 2D distance
        if (clickedFace == net.minecraft.core.Direction.UP || clickedFace == net.minecraft.core.Direction.DOWN) {
            // Horizontal plane - must be same Y, check X and Z distance
            return pos.getY() == center.getY() && Math.max(dx, dz) <= radius;
        } else if (clickedFace == net.minecraft.core.Direction.NORTH || clickedFace == net.minecraft.core.Direction.SOUTH) {
            // North/South plane - must be same Z, check X and Y distance
            return pos.getZ() == center.getZ() && Math.max(dx, dy) <= radius;
        } else {
            // East/West plane - must be same X, check Y and Z distance
            return pos.getX() == center.getX() && Math.max(dy, dz) <= radius;
        }
    }

    /**
     * Check if player has the block item in inventory
     */
    private boolean hasBlockInInventory(Player player, Block block) {
        ItemStack blockItem = new ItemStack(block.asItem());
        if (blockItem.isEmpty()) {
            return false;
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameTags(stack, blockItem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process active replacement operations
     * Called from event handler
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

            // Only process operations in this dimension
            if (!operation.level.equals(level)) {
                continue;
            }

            // Process blocks
            boolean finished = operation.processBlocks(blocksPerTick);
            if (finished) {
                iterator.remove();
            }
        }
    }

    // NBT Methods
    @SuppressWarnings("removal")
    private List<Block> getSelectedBlocks(ItemStack stack) {
        List<Block> blocks = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(NBT_SELECTED_BLOCKS)) {
            return blocks;
        }

        ListTag list = tag.getList(NBT_SELECTED_BLOCKS, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            String blockId = list.getString(i);
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
            if (block != null && block != Blocks.AIR) {
                blocks.add(block);
            }
        }

        return blocks;
    }

    private void setSelectedBlocks(ItemStack stack, List<Block> blocks) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = new ListTag();

        for (Block block : blocks) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id != null) {
                list.add(StringTag.valueOf(id.toString()));
            }
        }

        tag.put(NBT_SELECTED_BLOCKS, list);
    }

    private void clearSelectedBlocks(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(NBT_SELECTED_BLOCKS);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.EQUIVALENCY_1)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.EQUIVALENCY_2)
            .withStyle(ChatFormatting.GRAY));

        // Show current radius with area dimensions for clarity
        int radius = getRadius(stack);
        int areaDimension = (radius * 2) + 1; // e.g., radius 1 = 3x3, radius 2 = 5x5
        tooltip.add(Component.translatable("tooltip.animus.equivalency.radius_info", radius, areaDimension, areaDimension)
            .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.animus.equivalency.scroll_adjust", MAX_RADIUS)
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        // Show selected blocks
        List<Block> selected = getSelectedBlocks(stack);
        if (!selected.isEmpty()) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.EQUIVALENCY_SELECTED, selected.size(), MAX_SELECTED_BLOCKS)
                .withStyle(ChatFormatting.GOLD));

            // If shift is held, show all selected blocks (check client-side only)
            boolean showDetails = false;
            if (level != null && level.isClientSide()) {
                showDetails = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> InputClientHelper::isShiftDown);
            }
            if (showDetails) {
                for (Block block : selected) {
                    tooltip.add(Component.literal("  • ")
                        .append(block.getName())
                        .withStyle(ChatFormatting.GRAY));
                }
            } else {
                tooltip.add(Component.translatable("tooltip.animus.equivalency.hold_sneak")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    // Radius NBT methods
    private int getRadius(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int radius;
        if (tag == null || !tag.contains(NBT_RADIUS)) {
            // Default to config value
            radius = AnimusConfig.sigils.sigilEquivalencyRadius.get();
        } else {
            radius = tag.getInt(NBT_RADIUS);
        }
        // Always clamp to valid range to prevent bugs
        return Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
    }

    private void setRadius(ItemStack stack, int radius) {
        CompoundTag tag = stack.getOrCreateTag();
        // Clamp before saving
        radius = Math.max(MIN_RADIUS, Math.min(MAX_RADIUS, radius));
        tag.putInt(NBT_RADIUS, radius);
    }

    /**
     * Public method for the network packet handler to update radius on server side
     */
    public void setRadiusFromPacket(ItemStack stack, int radius) {
        setRadius(stack, radius);
    }

    /**
     * Tracks an ongoing block replacement operation
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
         * Process up to maxBlocks replacements
         * Returns true if operation is finished
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

            // Select random replacement block (filtered list only contains different blocks)
            Block replacementBlock = replacementBlocks.get(random.nextInt(replacementBlocks.size()));

            // Safety check: Skip if somehow the same (shouldn't happen due to pre-filtering)
            if (currentState.getBlock() == replacementBlock) {
                return;
            }

            // Get drops with silk touch
            ItemStack tool = new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
            tool.enchant(Enchantments.SILK_TOUCH, 1);

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
                    net.minecraft.core.particles.ParticleTypes.WITCH,
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
                if (ItemStack.isSameItemSameTags(stack, blockItem)) {
                    stack.shrink(1);
                    return true;
                }
            }
            return false;
        }
    }
}
