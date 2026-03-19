package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import com.teamdman.animus.compat.arsnouveau.BlockEntityArcaneRune;
import com.teamdman.animus.registry.AnimusBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import com.breakinblocks.neovitae.api.altar.rune.IAltarRuneType;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.blockentity.BloodAltarTile;
import com.breakinblocks.neovitae.common.tag.NVTags;
import com.breakinblocks.neovitae.common.registry.AltarComponent;
import com.breakinblocks.neovitae.common.structure.NVMultiblock;
import com.breakinblocks.neovitae.ritual.IMasterRitualStone;
import com.breakinblocks.neovitae.ritual.Ritual;
import com.breakinblocks.neovitae.util.AltarScanResult;
import com.breakinblocks.neovitae.util.AltarUtil;

import java.util.List;
import java.util.Map;

/**
 * Sanguine Diviner - Displays information about Blood Magic altars and rituals
 * <p>
 * Features:
 * - Right-click altar to check tier and capacity
 * - Shows current blood level, capacity, and tier
 * - Holding and looking at altar shows ghost blocks for next tier upgrade
 * - Sneak + Right-click altar to auto-place upgrade blocks from inventory
 * - Right-click ritual to show ritual information
 * - Sneak + Right-click ritual to dismantle it
 */
public class ItemSanguineDiviner extends Item {

    public ItemSanguineDiviner() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        // Check if clicked block is an Arcane Rune (Ars Nouveau compat)
        if (ModList.get().isLoaded("ars_nouveau") && blockEntity instanceof BlockEntityArcaneRune arcaneRune) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            displayArcaneRuneInfo(player, arcaneRune);

            // Play sound
            level.playSound(
                null,
                pos,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                0.5F,
                1.2F
            );

            return InteractionResult.SUCCESS;
        }

        // Check if clicked block is a Master Ritual Stone
        if (blockEntity instanceof IMasterRitualStone ritualStone) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            Ritual ritual = ritualStone.getCurrentRitual();
            boolean isActive = ritualStone.isActive();

            // Sneak + Right-click to dismantle ritual
            if (player.isShiftKeyDown()) {
                if (ritual != null) {
                    // Stop the ritual
                    ritualStone.stopRitual(Ritual.BreakType.DEACTIVATE);

                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.ritual_dismantled")
                            .withStyle(ChatFormatting.GOLD),
                        true
                    );

                    // Play sound
                    level.playSound(
                        null,
                        pos,
                        SoundEvents.GLASS_BREAK,
                        SoundSource.BLOCKS,
                        0.7F,
                        0.8F
                    );
                } else {
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.no_ritual_to_dismantle")
                            .withStyle(ChatFormatting.RED),
                        true
                    );
                }
                return InteractionResult.SUCCESS;
            }

            // Normal click - show ritual information
            if (ritual == null) {
                player.displayClientMessage(
                    Component.translatable("text.component.animus.diviner.no_ritual_set")
                        .withStyle(ChatFormatting.GRAY),
                    false
                );
            } else {
                String ritualName = ritual.getTranslationKey();
                player.displayClientMessage(
                    Component.translatable("text.component.animus.diviner.ritual_label").withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable(ritualName).withStyle(ChatFormatting.WHITE)),
                    false
                );
                player.displayClientMessage(
                    Component.translatable("text.component.animus.diviner.status_label").withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable(isActive ? "text.component.animus.diviner.status_active" : "text.component.animus.diviner.status_inactive")
                            .withStyle(isActive ? ChatFormatting.GREEN : ChatFormatting.RED)),
                    false
                );

                // Show owner if available
                java.util.UUID owner = ritualStone.getOwner();
                if (owner != null) {
                    // Try to get player name from server
                    String ownerName = owner.toString();
                    net.minecraft.server.level.ServerPlayer ownerPlayer = level.getServer() != null
                        ? level.getServer().getPlayerList().getPlayer(owner)
                        : null;
                    if (ownerPlayer != null) {
                        ownerName = ownerPlayer.getName().getString();
                    }

                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.owner_label").withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(ownerName)
                                .withStyle(ChatFormatting.YELLOW)),
                        false
                    );
                }

                player.displayClientMessage(
                    Component.translatable("text.component.animus.diviner.sneak_to_dismantle")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                    false
                );
            }

            // Play sound
            level.playSound(
                null,
                pos,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                0.5F,
                1.2F
            );

            return InteractionResult.SUCCESS;
        }

        // Check if clicked block is a Blood Altar
        if (blockEntity instanceof BloodAltarTile altar) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            int tierLevel = altar.getTier();

            // Sneak + Right-click to auto-place upgrade blocks
            if (player.isShiftKeyDown()) {
                int currentTier = tierLevel;
                int totalPlacedCount = 0;
                int highestTierBuilt = currentTier;

                // Keep building tiers until we hit max or can't place any more blocks
                while (true) {
                    int nextTier = currentTier + 1;

                    // Check if there's a next tier
                    if (nextTier >= NVMultiblock.TIER_LIST.length || NVMultiblock.TIER_LIST[nextTier] == null) {
                        break;
                    }

                    int placedCount = autoPlaceUpgradeBlocks(player, level, pos, nextTier);
                    totalPlacedCount += placedCount;

                    if (placedCount > 0) {
                        highestTierBuilt = nextTier;
                    }

                    // Move to next tier and continue
                    currentTier = nextTier;
                }

                if (totalPlacedCount > 0) {
                    // Display 1-indexed tier to match Blood Magic convention
                    int displayTier = highestTierBuilt + 1;
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.placed_blocks", totalPlacedCount, totalPlacedCount == 1 ? "" : "s", displayTier)
                            .withStyle(ChatFormatting.GREEN),
                        true
                    );

                    // Play success sound
                    level.playSound(
                        null,
                        pos,
                        SoundEvents.STONE_PLACE,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                    );
                } else if (tierLevel + 1 >= NVMultiblock.TIER_LIST.length || NVMultiblock.TIER_LIST[tierLevel + 1] == null) {
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.max_tier")
                            .withStyle(ChatFormatting.GOLD),
                        true
                    );
                } else {
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.no_blocks_to_place")
                            .withStyle(ChatFormatting.YELLOW),
                        true
                    );
                }

                return InteractionResult.SUCCESS;
            }

            // Normal click - show altar info
            int currentBlood = altar.getCurrentBlood();
            int capacity = altar.getMainCapacity();

            // Display information to player
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.DIVINER_ALTAR_INFO), false
            );
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.DIVINER_ESSENCE_INFO, currentBlood, capacity), false
            );

            // Show tier information (display as 1-indexed to match Blood Magic convention)
            int displayTier = tierLevel + 1;
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.DIVINER_TIER_INFO, displayTier), false
            );

            // Show rune breakdown for tier 1+ (tier is 0-indexed, so >= 1 means tier 2+)
            if (tierLevel >= 1) {
                displayRuneBreakdown(player, level, pos, tierLevel);
            }

            // Show altar multipliers
            displayAltarMultipliers(player, altar);

            int nextTier = tierLevel + 1;
            if (nextTier < NVMultiblock.TIER_LIST.length && NVMultiblock.TIER_LIST[nextTier] != null) {
                int nextDisplayTier = nextTier + 1;
                player.displayClientMessage(
                    Component.translatable("text.component.animus.diviner.sneak_to_build", nextDisplayTier)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                    false
                );
            } else {
                player.displayClientMessage(
                    Component.translatable("text.component.animus.diviner.max_tier")
                        .withStyle(ChatFormatting.GOLD),
                    false
                );
            }

            // Play sound
            level.playSound(
                null,
                pos,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                0.5F,
                1.0F
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    /**
     * Attempts to auto-place upgrade blocks for the next tier from the player's inventory.
     * In creative mode, places default blocks even without inventory items.
     *
     * @param player    The player
     * @param level     The world
     * @param altarPos  Position of the blood altar
     * @param nextTier  The tier to build towards
     * @return Number of blocks placed
     */
    private int autoPlaceUpgradeBlocks(Player player, Level level, BlockPos altarPos, int nextTier) {
        if (nextTier >= NVMultiblock.TIER_LIST.length || NVMultiblock.TIER_LIST[nextTier] == null) {
            return 0;
        }

        List<AltarComponent> components = NVMultiblock.TIER_LIST[nextTier].components();
        int placedCount = 0;
        boolean isCreative = player.isCreative();

        for (AltarComponent component : components) {
            BlockPos componentPos = altarPos.offset(component.pos());

            // Skip the altar position itself (0, 0, 0) - never replace the altar
            if (component.pos().equals(BlockPos.ZERO)) {
                continue;
            }

            BlockState existingState = level.getBlockState(componentPos);

            // Check if the block is already valid for this component
            boolean isValid = isValidBlockForComponent(component, existingState, level);

            // In creative mode, replace any block that isn't our preferred default
            // In survival mode, only place if the position is air/replaceable or has an invalid block
            if (!isCreative) {
                // Survival mode: skip if position has a non-replaceable valid block
                if (!existingState.isAir() && !existingState.canBeReplaced() && isValid) {
                    continue;
                }
            } else {
                // Creative mode: skip only if the block is already our preferred default block
                Block defaultBlock = getDefaultBlockForComponent(component, level);
                if (defaultBlock != null && existingState.is(defaultBlock)) {
                    continue;
                }
            }

            // Try to find and place a valid block from inventory first
            ItemStack matchingStack = findMatchingBlockInInventory(player, component, level);
            if (!matchingStack.isEmpty() && matchingStack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                BlockState newState = block.defaultBlockState();

                // Place the block
                level.setBlock(componentPos, newState, 3);

                // Consume the item if not in creative
                if (!isCreative) {
                    matchingStack.shrink(1);
                }

                placedCount++;
            } else if (isCreative) {
                // In creative mode, place a default block for this component
                Block defaultBlock = getDefaultBlockForComponent(component, level);
                if (defaultBlock != null) {
                    level.setBlock(componentPos, defaultBlock.defaultBlockState(), 3);
                    placedCount++;
                }
            }
        }

        return placedCount;
    }

    /**
     * Gets a default block to place for an altar component in creative mode.
     */
    private Block getDefaultBlockForComponent(AltarComponent component, Level level) {
        ResourceLocation materialId = component.material().id();
        String path = materialId.getPath().toLowerCase();

        // Check for runes tag (exact match or path-based)
        if (materialId.equals(NVTags.Blocks.RUNES.location()) || path.contains("rune")) {
            return NVBlocks.RUNE_BLANK.block().get();
        }

        // Check for pillars tag - use stone bricks (exact match or path-based)
        if (materialId.equals(NVTags.Blocks.PILLARS.location()) || path.contains("pillar")) {
            return Blocks.STONE_BRICKS;
        }

        // Check for tier-specific capstones (exact match first, then path-based fallback)
        // T6 capstones - Use Animus's Crystallized Demon Will blocks
        if (materialId.equals(NVTags.Blocks.T6_CAPSTONES.location()) ||
            path.contains("t6_capstone") || path.contains("tier6_capstone") || path.contains("tier_6_capstone")) {
            return AnimusBlocks.BLOCK_CRYSTALLIZED_DEMON_WILL.get();
        }
        // T5 capstones
        if (materialId.equals(NVTags.Blocks.T5_CAPSTONES.location()) ||
            path.contains("t5_capstone") || path.contains("tier5_capstone") || path.contains("tier_5_capstone")) {
            return NVBlocks.HELLFORGED_BLOCK.block().get();
        }
        // T4 capstones
        if (materialId.equals(NVTags.Blocks.T4_CAPSTONES.location()) ||
            path.contains("t4_capstone") || path.contains("tier4_capstone") || path.contains("tier_4_capstone")) {
            return NVBlocks.BLOODSTONE_BRICK.block().get();
        }
        // T3 capstones
        if (materialId.equals(NVTags.Blocks.T3_CAPSTONES.location()) ||
            path.contains("t3_capstone") || path.contains("tier3_capstone") || path.contains("tier_3_capstone")) {
            return Blocks.GLOWSTONE;
        }

        // Generic capstone fallback - check tier number in path
        if (path.contains("capstone")) {
            // Try to extract tier number
            if (path.contains("6")) {
                return AnimusBlocks.BLOCK_CRYSTALLIZED_DEMON_WILL.get();
            } else if (path.contains("5")) {
                return NVBlocks.HELLFORGED_BLOCK.block().get();
            } else if (path.contains("4")) {
                return NVBlocks.BLOODSTONE_BRICK.block().get();
            } else if (path.contains("3")) {
                return Blocks.GLOWSTONE;
            }
        }

        // Check for bloodstone
        if (path.contains("bloodstone")) {
            return NVBlocks.BLOODSTONE_BRICK.block().get();
        }

        // Check for crystal
        if (path.contains("crystal")) {
            return NVBlocks.CRYSTAL_CLUSTER.block().get();
        }

        // If it's a specific block (not a tag), try to get it directly
        if (!component.material().tag()) {
            Block block = level.registryAccess()
                    .registryOrThrow(Registries.BLOCK)
                    .get(ResourceKey.create(Registries.BLOCK, materialId));
            if (block != null) {
                return block;
            }
        }

        // Default fallback to stone bricks
        return Blocks.STONE_BRICKS;
    }

    /**
     * Checks if a block state is valid for the given altar component.
     */
    private boolean isValidBlockForComponent(AltarComponent component, BlockState state, Level level) {
        if (component.material().tag()) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, component.material().id());
            return state.is(tag);
        } else {
            Block block = level.registryAccess()
                    .registryOrThrow(Registries.BLOCK)
                    .get(ResourceKey.create(Registries.BLOCK, component.material().id()));
            return block != null && state.is(block);
        }
    }

    /**
     * Finds a block item in the player's inventory that matches the component requirements.
     */
    private ItemStack findMatchingBlockInInventory(Player player, AltarComponent component, Level level) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
                continue;
            }

            Block block = blockItem.getBlock();
            BlockState testState = block.defaultBlockState();

            if (isValidBlockForComponent(component, testState, level)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Displays information about an Arcane Rune (Ars Nouveau compat).
     */
    private void displayArcaneRuneInfo(Player player, BlockEntityArcaneRune arcaneRune) {
        player.displayClientMessage(
            Component.translatable("text.component.animus.diviner.arcane_rune_header")
                .withStyle(ChatFormatting.LIGHT_PURPLE),
            false
        );

        // Source level
        int currentSource = arcaneRune.getSource();
        int maxSource = arcaneRune.getMaxSource();
        player.displayClientMessage(
            Component.translatable("text.component.animus.diviner.arcane_rune_source", currentSource, maxSource)
                .withStyle(ChatFormatting.AQUA),
            false
        );

        // Powered state
        boolean hasPower = arcaneRune.hasSource();
        player.displayClientMessage(
            Component.translatable("text.component.animus.diviner.arcane_rune_powered")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.translatable(hasPower
                    ? "text.component.animus.diviner.arcane_rune_powered_yes"
                    : "text.component.animus.diviner.arcane_rune_powered_no")
                    .withStyle(hasPower ? ChatFormatting.GREEN : ChatFormatting.RED)),
            false
        );

        // Speed multiplier
        float speedMult = arcaneRune.getSpeedMultiplier();
        String speedPercent = String.format("%.0f%%", speedMult * 100);
        player.displayClientMessage(
            Component.translatable("text.component.animus.diviner.arcane_rune_speed", speedPercent)
                .withStyle(hasPower ? ChatFormatting.GREEN : ChatFormatting.YELLOW),
            false
        );

        // Dislocation bonus (only when powered)
        if (arcaneRune.providesDislocationBonus()) {
            player.displayClientMessage(
                Component.translatable("text.component.animus.diviner.arcane_rune_dislocation")
                    .withStyle(ChatFormatting.GREEN),
                false
            );
        }
    }

    /**
     * Displays the rune breakdown for an altar.
     */
    private void displayRuneBreakdown(Player player, Level level, BlockPos altarPos, int tier) {
        AltarScanResult scanResult = AltarUtil.scanForRunes(tier, level, altarPos);

        if (!scanResult.hasRunes()) {
            return;
        }

        player.displayClientMessage(
            Component.translatable("text.component.animus.diviner.rune_breakdown_header")
                .withStyle(ChatFormatting.DARK_PURPLE),
            false
        );

        // Display each rune type with count
        for (Map.Entry<IAltarRuneType, Integer> entry : scanResult.runeCounts().entrySet()) {
            String runeName = entry.getKey().getSerializedName();
            int count = entry.getValue();

            // Capitalize and format the rune name nicely
            String displayName = formatRuneName(runeName);

            player.displayClientMessage(
                Component.literal("  " + displayName + ": ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(count))
                        .withStyle(ChatFormatting.WHITE)),
                false
            );
        }
    }

    /**
     * Formats a rune name for display (e.g., "self_sacrifice" -> "Self Sacrifice").
     */
    private String formatRuneName(String runeName) {
        String[] words = runeName.split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }
        return result.toString();
    }

    /**
     * Displays altar multipliers from runes.
     */
    private void displayAltarMultipliers(Player player, BloodAltarTile altar) {
        float speedBonus = altar.getSpeedBonus();
        float dislocationBonus = altar.getDislocationBonus();
        float sacrificeBonus = altar.getSacrificeBonus();
        float selfSacrificeBonus = altar.getSelfSacrificeBonus();
        float orbCapacityBonus = altar.getOrbCapacityBonus();
        float capacityBonus = altar.getBonusCapacity();
        float efficiency = altar.getEfficiency();
        int tickRate = altar.getTickRate();

        // Only show multipliers section if there are any bonuses
        boolean hasAnyBonus = speedBonus != 0 || dislocationBonus != 1.0f ||
                              sacrificeBonus != 0 || selfSacrificeBonus != 0 ||
                              orbCapacityBonus != 0 || capacityBonus != 0 ||
                              efficiency != 1.0f || tickRate != 20;

        if (!hasAnyBonus) {
            return;
        }

        player.displayClientMessage(
            Component.translatable("text.component.animus.diviner.multipliers_header")
                .withStyle(ChatFormatting.GOLD),
            false
        );

        // Speed bonus (displayed as percentage)
        if (speedBonus != 0) {
            String speedStr = String.format("%+.0f%%", speedBonus * 100);
            player.displayClientMessage(
                Component.literal("  ")
                    .append(Component.translatable("text.component.animus.diviner.multiplier_speed"))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(speedStr)
                        .withStyle(speedBonus > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                false
            );
        }

        // Dislocation bonus (displayed as multiplier)
        if (dislocationBonus != 1.0f) {
            String dislocationStr = String.format("%.2fx", dislocationBonus);
            player.displayClientMessage(
                Component.literal("  ")
                    .append(Component.translatable("text.component.animus.diviner.multiplier_dislocation"))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(dislocationStr)
                        .withStyle(dislocationBonus > 1 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                false
            );
        }

        // Sacrifice bonus
        if (sacrificeBonus != 0) {
            String sacrificeStr = String.format("%+.0f%%", sacrificeBonus * 100);
            player.displayClientMessage(
                Component.literal("  ")
                    .append(Component.translatable("text.component.animus.diviner.multiplier_sacrifice"))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(sacrificeStr)
                        .withStyle(sacrificeBonus > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                false
            );
        }

        // Self-sacrifice bonus
        if (selfSacrificeBonus != 0) {
            String selfSacrificeStr = String.format("%+.0f%%", selfSacrificeBonus * 100);
            player.displayClientMessage(
                Component.literal("  ")
                    .append(Component.translatable("text.component.animus.diviner.multiplier_self_sacrifice"))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(selfSacrificeStr)
                        .withStyle(selfSacrificeBonus > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                false
            );
        }

        // Orb capacity bonus
        if (orbCapacityBonus != 0) {
            String orbStr = String.format("%+.0f%%", orbCapacityBonus * 100);
            player.displayClientMessage(
                Component.literal("  ")
                    .append(Component.translatable("text.component.animus.diviner.multiplier_orb"))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(orbStr)
                        .withStyle(orbCapacityBonus > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                false
            );
        }

        // Capacity bonus
        if (capacityBonus != 0) {
            String capacityStr = String.format("%+.0f%%", capacityBonus * 100);
            player.displayClientMessage(
                Component.literal("  ")
                    .append(Component.translatable("text.component.animus.diviner.multiplier_capacity"))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(capacityStr)
                        .withStyle(capacityBonus > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                false
            );
        }

        // Efficiency (lower is better - less LP lost when paused)
        if (efficiency != 1.0f) {
            String efficiencyStr = String.format("%.0f%%", efficiency * 100);
            player.displayClientMessage(
                Component.literal("  ")
                    .append(Component.translatable("text.component.animus.diviner.multiplier_efficiency"))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(efficiencyStr)
                        .withStyle(efficiency < 1 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                false
            );
        }

        // Tick rate (lower is better - faster operations)
        if (tickRate != 20) {
            player.displayClientMessage(
                Component.literal("  ")
                    .append(Component.translatable("text.component.animus.diviner.multiplier_tick_rate"))
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(tickRate))
                        .withStyle(tickRate < 20 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                false
            );
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_FIRST));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_SECOND));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_THIRD));
        tooltip.add(Component.translatable("tooltip.animus.diviner.ghost_blocks").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("tooltip.animus.diviner.auto_build").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("tooltip.animus.diviner.ritual_info").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.animus.diviner.ritual_dismantle").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
