package com.teamdman.animus.items;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import wayoftime.bloodmagic.ritual.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Ritual Designer - Dev tool for creating Blood Magic rituals
 * <p>
 * Usage:
 * 1. Shift + Right-click a block to set corner 1
 * 2. Shift + Right-click another block to set corner 2
 * 3. Right-click a Master Ritual Stone to scan the area and generate code
 * 4. Code is automatically copied to clipboard!
 * <p>
 * Features:
 * - Scans ONLY Blood Magic ritual stones between corner 1 and corner 2
 * - Ignores air, master ritual stone, and all other non-ritual blocks
 * - Generates gatherComponents() Java code with relative positions
 * - Checks for ritual pattern conflicts with existing rituals
 * - Automatically copies code to clipboard
 * - Displays generated code in chat (requires operator permissions)
 * <p>
 * Note: Only ritual stones are recorded. Decorative blocks, air, and
 * any other blocks are completely ignored during the scan.
 */
public class ItemRitualDesigner extends Item {
    private static final String TAG_POS1 = "Pos1";
    private static final String TAG_POS2 = "Pos2";
    private static final String TAG_HAS_POS1 = "HasPos1";
    private static final String TAG_HAS_POS2 = "HasPos2";

    // Map of Blood Magic rune blocks to their EnumRuneType
    private static final Map<Block, String> RUNE_TYPES = new HashMap<>();

    static {
        // Initialize rune type mappings by checking Blood Magic's registry
        // These will be populated at runtime when first used
    }

    public ItemRitualDesigner() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // OP-only check
        if (!player.hasPermissions(2)) {
            return net.minecraft.world.InteractionResultHolder.fail(stack);
        }

        // Shift + Right-click air: Clear positions
        if (player.isShiftKeyDown()) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putBoolean(TAG_HAS_POS1, false);
            tag.putBoolean(TAG_HAS_POS2, false);

            player.displayClientMessage(
                Component.literal("Positions cleared!")
                    .withStyle(ChatFormatting.YELLOW),
                true
            );

            if (!level.isClientSide) {
                level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS,
                    0.5F,
                    1.0F
                );
            }

            return net.minecraft.world.InteractionResultHolder.success(stack);
        }

        return net.minecraft.world.InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        // OP-only check
        if (!player.hasPermissions(2)) {
            player.displayClientMessage(
                Component.literal("Ritual Designer requires operator permissions")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockEntity blockEntity = level.getBlockEntity(clickedPos);

        // Shift + Right-click: Set positions
        if (player.isShiftKeyDown()) {
            CompoundTag tag = stack.getOrCreateTag();

            // Set position 1 if not set, otherwise set position 2
            if (!tag.getBoolean(TAG_HAS_POS1)) {
                tag.putLong(TAG_POS1, clickedPos.asLong());
                tag.putBoolean(TAG_HAS_POS1, true);
                tag.putBoolean(TAG_HAS_POS2, false); // Reset pos2 when setting pos1

                player.displayClientMessage(
                    Component.literal("Corner 1 set to: ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(clickedPos.toShortString())
                            .withStyle(ChatFormatting.WHITE)),
                    true
                );

                level.playSound(
                    null,
                    clickedPos,
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.BLOCKS,
                    0.5F,
                    1.0F
                );
            } else if (!tag.getBoolean(TAG_HAS_POS2)) {
                tag.putLong(TAG_POS2, clickedPos.asLong());
                tag.putBoolean(TAG_HAS_POS2, true);

                player.displayClientMessage(
                    Component.literal("Corner 2 set to: ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(clickedPos.toShortString())
                            .withStyle(ChatFormatting.WHITE)),
                    true
                );

                level.playSound(
                    null,
                    clickedPos,
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.BLOCKS,
                    0.5F,
                    1.2F
                );
            } else {
                // Both positions are set, reset to position 1
                tag.putLong(TAG_POS1, clickedPos.asLong());
                tag.putBoolean(TAG_HAS_POS1, true);
                tag.putBoolean(TAG_HAS_POS2, false);

                player.displayClientMessage(
                    Component.literal("Reset! Corner 1 set to: ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(clickedPos.toShortString())
                            .withStyle(ChatFormatting.WHITE)),
                    true
                );

                level.playSound(
                    null,
                    clickedPos,
                    SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.BLOCKS,
                    0.5F,
                    0.8F
                );
            }

            return InteractionResult.SUCCESS;
        }

        // Normal Right-click on Master Ritual Stone: Generate code
        if (blockEntity instanceof IMasterRitualStone) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            CompoundTag tag = stack.getOrCreateTag();

            // Check if both positions are set
            if (!tag.getBoolean(TAG_HAS_POS1) || !tag.getBoolean(TAG_HAS_POS2)) {
                player.displayClientMessage(
                    Component.literal("Please set both corners first!")
                        .withStyle(ChatFormatting.RED),
                    true
                );
                player.displayClientMessage(
                    Component.literal("Shift + Right-click opposite corners of ritual area")
                        .withStyle(ChatFormatting.GRAY),
                    false
                );
                return InteractionResult.FAIL;
            }

            BlockPos pos1 = BlockPos.of(tag.getLong(TAG_POS1));
            BlockPos pos2 = BlockPos.of(tag.getLong(TAG_POS2));
            BlockPos masterPos = clickedPos;

            // Scan the area and generate ritual code
            String code = generateRitualCode(level, pos1, pos2, masterPos, player);

            if (code != null) {
                // Send code to clipboard
                if (player instanceof ServerPlayer serverPlayer) {
                    // Send packet to copy to clipboard
                    com.teamdman.animus.network.RitualCodePacket packet = new com.teamdman.animus.network.RitualCodePacket(code);
                    com.teamdman.animus.network.AnimusNetwork.CHANNEL.send(
                        net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> serverPlayer),
                        packet
                    );

                    player.displayClientMessage(
                        Component.literal("Ritual code copied to clipboard!")
                            .withStyle(ChatFormatting.GREEN),
                        true
                    );

                    // Also log the code
                    player.sendSystemMessage(Component.literal("=== RITUAL CODE START ===").withStyle(ChatFormatting.GOLD));
                    for (String line : code.split("\n")) {
                        player.sendSystemMessage(Component.literal(line).withStyle(ChatFormatting.WHITE));
                    }
                    player.sendSystemMessage(Component.literal("=== RITUAL CODE END ===").withStyle(ChatFormatting.GOLD));
                }

                level.playSound(
                    null,
                    masterPos,
                    SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
                );
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private String generateRitualCode(Level level, BlockPos pos1, BlockPos pos2, BlockPos masterPos, Player player) {
        // Initialize rune types if not already done
        if (RUNE_TYPES.isEmpty()) {
            initializeRuneTypes(level);

            // Debug: Show how many rune types were found
            player.displayClientMessage(
                Component.literal("Initialized " + RUNE_TYPES.size() + " Blood Magic rune types")
                    .withStyle(ChatFormatting.GRAY),
                false
            );
        }

        // Calculate bounding box
        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        // Debug: Show bounding box info
        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        player.displayClientMessage(
            Component.literal("Scanning area: " + sizeX + "x" + sizeY + "x" + sizeZ +
                " (" + (sizeX * sizeY * sizeZ) + " blocks)")
                .withStyle(ChatFormatting.GRAY),
            false
        );

        // Find all runes in the area
        List<RuneData> runes = new ArrayList<>();
        Map<String, Integer> nonRuneBlockCounts = new HashMap<>();
        int totalBlocksChecked = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    // Skip the master ritual stone itself
                    if (pos.equals(masterPos)) {
                        continue;
                    }

                    Block block = level.getBlockState(pos).getBlock();
                    totalBlocksChecked++;

                    // Skip air and other non-rune blocks explicitly
                    if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) {
                        continue;
                    }

                    // Check if it's a ritual rune block (only Blood Magic rune blocks are recorded)
                    String runeType = RUNE_TYPES.get(block);
                    if (runeType != null) {
                        // Calculate relative position from master stone
                        int relX = x - masterPos.getX();
                        int relY = y - masterPos.getY();
                        int relZ = z - masterPos.getZ();

                        runes.add(new RuneData(relX, relY, relZ, runeType));
                    } else {
                        // Track non-rune blocks for debugging
                        String blockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block).toString();
                        nonRuneBlockCounts.put(blockId, nonRuneBlockCounts.getOrDefault(blockId, 0) + 1);
                    }
                }
            }
        }

        // Debug: Show what was found
        if (runes.isEmpty() && !nonRuneBlockCounts.isEmpty()) {
            player.displayClientMessage(
                Component.literal("No runes found! Found these blocks instead:")
                    .withStyle(ChatFormatting.YELLOW),
                false
            );

            // Show up to 5 most common non-rune blocks
            nonRuneBlockCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    player.displayClientMessage(
                        Component.literal("  - " + entry.getKey() + " (" + entry.getValue() + "x)")
                            .withStyle(ChatFormatting.GRAY),
                        false
                    );
                });
        }

        if (runes.isEmpty()) {
            player.displayClientMessage(
                Component.literal("No rune blocks found in the selected area!")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return null;
        }

        // Check for conflicts with existing rituals
        String conflict = checkRitualConflict(runes, level);
        if (conflict != null) {
            player.displayClientMessage(
                Component.literal("CONFLICT: This pattern matches ritual: ")
                    .withStyle(ChatFormatting.RED)
                    .append(Component.literal(conflict).withStyle(ChatFormatting.YELLOW)),
                true
            );
            player.displayClientMessage(
                Component.literal("Please modify the ritual structure to make it unique.")
                    .withStyle(ChatFormatting.GRAY),
                false
            );
            return null;
        }

        // Generate the code
        return generateCode(runes, player);
    }

    private void initializeRuneTypes(Level level) {
        // Map exact Blood Magic ritual stone block IDs to their EnumRuneType
        // These correspond directly to the EnumRuneType enum in Blood Magic

        // Define the exact ritual stone block IDs
        Map<String, String> ritualStoneMap = Map.of(
            "bloodmagic:blankritualstone", "EnumRuneType.BLANK",
            "bloodmagic:waterritualstone", "EnumRuneType.WATER",
            "bloodmagic:airritualstone", "EnumRuneType.AIR",
            "bloodmagic:earthritualstone", "EnumRuneType.EARTH",
            "bloodmagic:fireritualstone", "EnumRuneType.FIRE",
            "bloodmagic:duskritualstone", "EnumRuneType.DUSK",
            "bloodmagic:dawnritualstone", "EnumRuneType.DAWN"
        );

        // Find and map each ritual stone block
        for (Map.Entry<String, String> entry : ritualStoneMap.entrySet()) {
            String blockId = entry.getKey();
            String runeType = entry.getValue();

            Block block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                new net.minecraft.resources.ResourceLocation(blockId)
            );

            if (block != null && block != Blocks.AIR) {
                RUNE_TYPES.put(block, runeType);
            }
        }
    }

    private String checkRitualConflict(List<RuneData> runes, Level level) {
        try {
            // Create a RitualManager instance and discover all registered rituals
            RitualManager ritualManager = new RitualManager();
            ritualManager.discover();

            // Get all registered rituals
            Collection<Ritual> allRituals = ritualManager.getRituals();

            // Convert our scanned runes to a comparable format
            Set<String> scannedPattern = runes.stream()
                .map(r -> r.x + "," + r.y + "," + r.z + "," + r.type)
                .collect(Collectors.toSet());

            // Check each ritual for pattern match
            for (Ritual ritual : allRituals) {
                try {
                    // Collect components from the ritual
                    List<RitualComponent> components = new ArrayList<>();
                    ritual.gatherComponents(components::add);

                    // Convert ritual components to comparable format
                    Set<String> ritualPattern = new HashSet<>();
                    for (RitualComponent component : components) {
                        BlockPos offset = component.getOffset();
                        String runeTypeName = component.getRuneType().name();
                        ritualPattern.add(offset.getX() + "," + offset.getY() + "," + offset.getZ() + ",EnumRuneType." + runeTypeName);
                    }

                    // Check if patterns match
                    if (scannedPattern.equals(ritualPattern)) {
                        // Found a conflict! Return the ritual name
                        String ritualId = ritualManager.getId(ritual);
                        String ritualName = ritual.getTranslationKey();
                        return ritualName + " (" + ritualId + ")";
                    }
                } catch (Exception e) {
                    // Skip this ritual if we can't check it
                    continue;
                }
            }

            return null; // No conflict detected
        } catch (Exception e) {
            // If we can't check, assume no conflict
            return null;
        }
    }

    private String generateCode(List<RuneData> runes, Player player) {
        StringBuilder code = new StringBuilder();

        code.append("@Override\n");
        code.append("public void gatherComponents(Consumer<RitualComponent> components) {\n");

        // Group runes by layer to make the code cleaner
        Map<Integer, List<RuneData>> runesByLayer = runes.stream()
            .collect(Collectors.groupingBy(r -> r.y));

        List<Integer> sortedLayers = new ArrayList<>(runesByLayer.keySet());
        Collections.sort(sortedLayers);

        // Check if we can use a loop pattern (all layers have the same pattern)
        boolean canUseLoop = false;
        if (sortedLayers.size() > 1) {
            canUseLoop = checkIfLayersAreIdentical(runesByLayer, sortedLayers);
        }

        if (canUseLoop && sortedLayers.size() > 1) {
            // Use for loop
            int minLayer = sortedLayers.get(0);
            int maxLayer = sortedLayers.get(sortedLayers.size() - 1);
            code.append("    for (int layer = ").append(minLayer).append("; layer < ").append(maxLayer + 1).append("; layer++) {\n");

            // Get runes from first layer as template
            List<RuneData> templateRunes = runesByLayer.get(sortedLayers.get(0));
            for (RuneData rune : templateRunes) {
                code.append("        addRune(components, ")
                    .append(rune.x).append(", layer, ")
                    .append(rune.z).append(", ")
                    .append(rune.type).append(");\n");
            }

            code.append("    }\n");
        } else {
            // Output each rune individually
            for (RuneData rune : runes) {
                code.append("    addRune(components, ")
                    .append(rune.x).append(", ")
                    .append(rune.y).append(", ")
                    .append(rune.z).append(", ")
                    .append(rune.type).append(");\n");
            }
        }

        code.append("}\n");

        // Add statistics
        player.displayClientMessage(
            Component.literal("Found " + runes.size() + " rune blocks")
                .withStyle(ChatFormatting.AQUA),
            false
        );

        return code.toString();
    }

    private boolean checkIfLayersAreIdentical(Map<Integer, List<RuneData>> runesByLayer, List<Integer> sortedLayers) {
        if (sortedLayers.size() < 2) {
            return false;
        }

        // Get the first layer as template
        List<RuneData> template = runesByLayer.get(sortedLayers.get(0));

        // Check if all other layers have the same pattern (ignoring Y coordinate)
        for (int i = 1; i < sortedLayers.size(); i++) {
            List<RuneData> currentLayer = runesByLayer.get(sortedLayers.get(i));

            if (template.size() != currentLayer.size()) {
                return false;
            }

            // Create sets of (x, z, type) for comparison
            Set<String> templateSet = template.stream()
                .map(r -> r.x + "," + r.z + "," + r.type)
                .collect(Collectors.toSet());

            Set<String> currentSet = currentLayer.stream()
                .map(r -> r.x + "," + r.z + "," + r.type)
                .collect(Collectors.toSet());

            if (!templateSet.equals(currentSet)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Dev Tool - Requires OP").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        tooltip.add(Component.literal("Shift + Right-click block: Set corner 1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + Right-click block: Set corner 2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("  (Select opposite corners of ritual area)").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Right-click Master Stone: Generate code").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + Right-click air: Clear positions").withStyle(ChatFormatting.GRAY));

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getBoolean(TAG_HAS_POS1)) {
            BlockPos pos1 = BlockPos.of(tag.getLong(TAG_POS1));
            tooltip.add(Component.literal("Corner 1: " + pos1.toShortString()).withStyle(ChatFormatting.GREEN));
        }
        if (tag.getBoolean(TAG_HAS_POS2)) {
            BlockPos pos2 = BlockPos.of(tag.getLong(TAG_POS2));
            tooltip.add(Component.literal("Corner 2: " + pos2.toShortString()).withStyle(ChatFormatting.GREEN));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    /**
     * Helper class to store rune data
     */
    private static class RuneData {
        final int x, y, z;
        final String type;

        RuneData(int x, int y, int z, String type) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
        }
    }
}
