package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
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
 * - Shift-right-click block: Add to selection list (up to 20)
 * - Shift-right-click air: Clear selection
 * - Right-click block: Replace all matching blocks in radius with random selection
 */
public class ItemSigilEquivalency extends AnimusSigilBase implements IBindable {
    private static final String NBT_SELECTED_BLOCKS = "SelectedBlocks";
    private static final int MAX_SELECTED_BLOCKS = 20;

    // Track ongoing replacement operations
    private static final Map<UUID, ReplacementOperation> activeOperations = new ConcurrentHashMap<>();

    public ItemSigilEquivalency() {
        super(Constants.Sigils.EQUIVALENCY, 0); // LP cost per block
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift-right-click air to clear selection
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

        if (level.isClientSide || player == null) {
            return InteractionResult.PASS;
        }

        BlockState targetState = level.getBlockState(pos);
        if (targetState.isAir()) {
            return InteractionResult.FAIL;
        }

        // Shift-right-click to add block to selection
        if (player.isShiftKeyDown()) {
            return handleBlockSelection(level, player, stack, targetState);
        }

        // Right-click to perform replacement
        return handleBlockReplacement((ServerLevel) level, (ServerPlayer) player, stack, pos, targetState);
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

        // Check if player has the block in inventory
        if (!hasBlockInInventory(player, targetBlock)) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.EQUIVALENCY_NO_BLOCKS)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
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

        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleBlockReplacement(ServerLevel level, ServerPlayer player, ItemStack stack,
                                                      BlockPos centerPos, BlockState targetState) {
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

        // Filter selected blocks that are in inventory
        List<Block> availableBlocks = new ArrayList<>();
        for (Block block : selectedBlocks) {
            if (hasBlockInInventory(player, block)) {
                availableBlocks.add(block);
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
        int radius = AnimusConfig.sigils.sigilEquivalencyRadius.get();
        List<BlockPos> matchingBlocks = findMatchingBlocksInRadius(level, centerPos, targetState.getBlock(), radius);

        if (matchingBlocks.isEmpty()) {
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.EQUIVALENCY_NO_MATCHES)
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        // Calculate total LP cost
        int lpPerBlock = AnimusConfig.sigils.sigilEquivalencyLPCost.get();
        int totalLP = matchingBlocks.size() * lpPerBlock;

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
            matchingBlocks,
            availableBlocks,
            targetState.getBlock()
        );
        activeOperations.put(player.getUUID(), operation);

        player.displayClientMessage(
            Component.translatable(
                Constants.Localizations.Text.EQUIVALENCY_STARTED,
                matchingBlocks.size()
            ).withStyle(ChatFormatting.GOLD),
            true
        );

        return InteractionResult.SUCCESS;
    }

    /**
     * Find all blocks matching the target in a radius using spiral pattern
     */
    private List<BlockPos> findMatchingBlocksInRadius(ServerLevel level, BlockPos center, Block targetBlock, int radius) {
        List<BlockPos> matches = new ArrayList<>();

        // Spiral pattern search
        for (int r = 0; r <= radius; r++) {
            // Search perimeter at radius r
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        // Only process blocks on the perimeter
                        if (Math.abs(dx) != r && Math.abs(dy) != r && Math.abs(dz) != r) {
                            continue;
                        }

                        BlockPos checkPos = center.offset(dx, dy, dz);
                        BlockState state = level.getBlockState(checkPos);

                        if (state.getBlock() == targetBlock) {
                            matches.add(checkPos.immutable());
                        }
                    }
                }
            }
        }

        return matches;
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

        // Show selected blocks
        List<Block> selected = getSelectedBlocks(stack);
        if (!selected.isEmpty()) {
            tooltip.add(Component.translatable(Constants.Localizations.Tooltips.EQUIVALENCY_SELECTED, selected.size(), MAX_SELECTED_BLOCKS)
                .withStyle(ChatFormatting.GOLD));
        }

        super.appendHoverText(stack, level, tooltip, flag);
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
        private int currentIndex = 0;
        private final Random random = new Random();

        public ReplacementOperation(ServerLevel level, UUID playerUUID, List<BlockPos> positions,
                                   List<Block> replacementBlocks, Block originalBlock) {
            this.level = level;
            this.playerUUID = playerUUID;
            this.positions = positions;
            this.replacementBlocks = replacementBlocks;
            this.originalBlock = originalBlock;
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

            // Get drops with silk touch
            ItemStack tool = new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE);
            tool.enchant(Enchantments.SILK_TOUCH, 1);

            LootParams.Builder lootBuilder = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, tool)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, player)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, level.getBlockEntity(pos));

            List<ItemStack> drops = currentState.getDrops(lootBuilder);

            // Give drops to player
            for (ItemStack drop : drops) {
                if (!player.getInventory().add(drop)) {
                    player.drop(drop, false);
                }
            }

            // Select random replacement block
            Block replacementBlock = replacementBlocks.get(random.nextInt(replacementBlocks.size()));

            // Consume block from inventory
            if (!consumeBlockFromInventory(player, replacementBlock)) {
                return; // No blocks left, skip
            }

            // Place new block
            BlockState newState = replacementBlock.defaultBlockState();
            level.setBlock(pos, newState, 3);

            // Play sound and particles
            level.playSound(null, pos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 0.3f, 2.0f);

            // Send particles
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.WITCH,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                10, 0.5, 0.5, 0.5, 0.05
            );
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
