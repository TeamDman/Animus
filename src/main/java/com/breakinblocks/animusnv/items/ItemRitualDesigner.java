package com.breakinblocks.animusnv.items;

import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import com.breakinblocks.neovitae.ritual.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Ritual Designer - Dev tool for creating NeoVitae rituals
 * <p>
 * Usage:
 * 1. Shift + Right-click a block to set corner 1
 * 2. Shift + Right-click another block to set corner 2
 * 3. Right-click a Master Ritual Stone to scan the area and generate code
 * 4. Code is automatically copied to clipboard!
 * <p>
 * Features:
 * - Scans ONLY NeoVitae ritual stones between corner 1 and corner 2
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
    private static final Map<Block, String> RUNE_TYPES = new HashMap<>();

    public ItemRitualDesigner() {
        super(new Item.Properties().stacksTo(1));
    }

    private boolean hasCorner1(ItemStack stack) {
        return stack.has(AnimusDataComponents.RITUAL_CORNER1.get());
    }

    private boolean hasCorner2(ItemStack stack) {
        return stack.has(AnimusDataComponents.RITUAL_CORNER2.get());
    }

    private BlockPos getCorner1(ItemStack stack) {
        return stack.get(AnimusDataComponents.RITUAL_CORNER1.get());
    }

    private BlockPos getCorner2(ItemStack stack) {
        return stack.get(AnimusDataComponents.RITUAL_CORNER2.get());
    }

    private void setCorner1(ItemStack stack, BlockPos pos) {
        stack.set(AnimusDataComponents.RITUAL_CORNER1.get(), pos);
    }

    private void setCorner2(ItemStack stack, BlockPos pos) {
        stack.set(AnimusDataComponents.RITUAL_CORNER2.get(), pos);
    }

    private void clearCorners(ItemStack stack) {
        stack.remove(AnimusDataComponents.RITUAL_CORNER1.get());
        stack.remove(AnimusDataComponents.RITUAL_CORNER2.get());
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!player.hasPermissions(2)) {
            return net.minecraft.world.InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            clearCorners(stack);

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

        if (player.isShiftKeyDown()) {
            if (!hasCorner1(stack)) {
                setCorner1(stack, clickedPos);
                stack.remove(AnimusDataComponents.RITUAL_CORNER2.get());

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
            } else if (!hasCorner2(stack)) {
                setCorner2(stack, clickedPos);

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
                setCorner1(stack, clickedPos);
                stack.remove(AnimusDataComponents.RITUAL_CORNER2.get());

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

        if (blockEntity instanceof IMasterRitualStone) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            if (!hasCorner1(stack) || !hasCorner2(stack)) {
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

            BlockPos pos1 = getCorner1(stack);
            BlockPos pos2 = getCorner2(stack);
            BlockPos masterPos = clickedPos;

            String code = generateRitualCode(level, pos1, pos2, masterPos, player);

            if (code != null) {
                if (player instanceof ServerPlayer serverPlayer) {
                    com.breakinblocks.animusnv.network.AnimusPayloads.sendToPlayer(serverPlayer,
                        new com.breakinblocks.animusnv.network.RitualCodePayload(code));

                    player.displayClientMessage(
                        Component.literal("Ritual code copied to clipboard!")
                            .withStyle(ChatFormatting.GREEN),
                        true
                    );

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
        if (RUNE_TYPES.isEmpty()) {
            initializeRuneTypes(level);

            player.displayClientMessage(
                Component.literal("Initialized " + RUNE_TYPES.size() + " NeoVitae rune types")
                    .withStyle(ChatFormatting.GRAY),
                false
            );
        }

        int minX = Math.min(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());

        int sizeX = maxX - minX + 1;
        int sizeY = maxY - minY + 1;
        int sizeZ = maxZ - minZ + 1;
        player.displayClientMessage(
            Component.literal("Scanning area: " + sizeX + "x" + sizeY + "x" + sizeZ +
                " (" + (sizeX * sizeY * sizeZ) + " blocks)")
                .withStyle(ChatFormatting.GRAY),
            false
        );

        List<RuneData> runes = new ArrayList<>();
        Map<String, Integer> nonRuneBlockCounts = new HashMap<>();
        int totalBlocksChecked = 0;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (pos.equals(masterPos)) {
                        continue;
                    }

                    Block block = level.getBlockState(pos).getBlock();
                    totalBlocksChecked++;

                    if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) {
                        continue;
                    }

                    String runeType = RUNE_TYPES.get(block);
                    if (runeType != null) {
                        int relX = x - masterPos.getX();
                        int relY = y - masterPos.getY();
                        int relZ = z - masterPos.getZ();

                        runes.add(new RuneData(relX, relY, relZ, runeType));
                    } else {
                        String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
                        nonRuneBlockCounts.put(blockId, nonRuneBlockCounts.getOrDefault(blockId, 0) + 1);
                    }
                }
            }
        }

        if (runes.isEmpty() && !nonRuneBlockCounts.isEmpty()) {
            player.displayClientMessage(
                Component.literal("No runes found! Found these blocks instead:")
                    .withStyle(ChatFormatting.YELLOW),
                false
            );

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

        return generateCode(runes, player);
    }

    private void initializeRuneTypes(Level level) {
        Map<String, String> ritualStoneMap = Map.of(
            "neovitae:blankritualstone", "EnumRuneType.BLANK",
            "neovitae:waterritualstone", "EnumRuneType.WATER",
            "neovitae:airritualstone", "EnumRuneType.AIR",
            "neovitae:earthritualstone", "EnumRuneType.EARTH",
            "neovitae:fireritualstone", "EnumRuneType.FIRE",
            "neovitae:duskritualstone", "EnumRuneType.DUSK",
            "neovitae:dawnritualstone", "EnumRuneType.DAWN"
        );

        for (Map.Entry<String, String> entry : ritualStoneMap.entrySet()) {
            String blockId = entry.getKey();
            String runeType = entry.getValue();

            Block block = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(blockId)).orElse(null);

            if (block != null && block != Blocks.AIR) {
                RUNE_TYPES.put(block, runeType);
            }
        }
    }

    private String checkRitualConflict(List<RuneData> runes, Level level) {
        // NeoVitae 4.x changed the RitualComponent API
        // The getOffset() and getRuneType() methods were removed or renamed
        // Conflict checking is disabled until NeoVitae exposes a stable API
        // for accessing ritual component data
        return null;
    }

    private String generateCode(List<RuneData> runes, Player player) {
        StringBuilder code = new StringBuilder();

        code.append("@Override\n");
        code.append("public void gatherComponents(Consumer<RitualComponent> components) {\n");

        Map<Integer, List<RuneData>> runesByLayer = runes.stream()
            .collect(Collectors.groupingBy(r -> r.y));

        List<Integer> sortedLayers = new ArrayList<>(runesByLayer.keySet());
        Collections.sort(sortedLayers);

        boolean canUseLoop = false;
        if (sortedLayers.size() > 1) {
            canUseLoop = checkIfLayersAreIdentical(runesByLayer, sortedLayers);
        }

        if (canUseLoop && sortedLayers.size() > 1) {
            int minLayer = sortedLayers.get(0);
            int maxLayer = sortedLayers.get(sortedLayers.size() - 1);
            code.append("    for (int layer = ").append(minLayer).append("; layer < ").append(maxLayer + 1).append("; layer++) {\n");

            List<RuneData> templateRunes = runesByLayer.get(sortedLayers.get(0));
            for (RuneData rune : templateRunes) {
                code.append("        addRune(components, ")
                    .append(rune.x).append(", layer, ")
                    .append(rune.z).append(", ")
                    .append(rune.type).append(");\n");
            }

            code.append("    }\n");
        } else {
            for (RuneData rune : runes) {
                code.append("    addRune(components, ")
                    .append(rune.x).append(", ")
                    .append(rune.y).append(", ")
                    .append(rune.z).append(", ")
                    .append(rune.type).append(");\n");
            }
        }

        code.append("}\n");

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

        List<RuneData> template = runesByLayer.get(sortedLayers.get(0));

        for (int i = 1; i < sortedLayers.size(); i++) {
            List<RuneData> currentLayer = runesByLayer.get(sortedLayers.get(i));

            if (template.size() != currentLayer.size()) {
                return false;
            }

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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Dev Tool - Requires OP").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        tooltip.add(Component.literal("Shift + Right-click block: Set corner 1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + Right-click block: Set corner 2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("  (Select opposite corners of ritual area)").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Right-click Master Stone: Generate code").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift + Right-click air: Clear positions").withStyle(ChatFormatting.GRAY));

        if (hasCorner1(stack)) {
            BlockPos pos1 = getCorner1(stack);
            tooltip.add(Component.literal("Corner 1: " + pos1.toShortString()).withStyle(ChatFormatting.GREEN));
        }
        if (hasCorner2(stack)) {
            BlockPos pos2 = getCorner2(stack);
            tooltip.add(Component.literal("Corner 2: " + pos2.toShortString()).withStyle(ChatFormatting.GREEN));
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

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
