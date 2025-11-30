package com.teamdman.animus.items;

import com.teamdman.animus.Constants;
import com.teamdman.animus.network.AltarGhostBlocksPacket;
import com.teamdman.animus.network.AnimusNetwork;
import com.teamdman.animus.util.AltarUpgradeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;
import wayoftime.bloodmagic.altar.AltarTier;
import wayoftime.bloodmagic.common.tile.TileAltar;
import wayoftime.bloodmagic.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.ritual.Ritual;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sanguine Diviner - Displays information about Blood Magic altars and rituals
 * <p>
 * Features:
 * - Right-click altar to check tier and capacity
 * - Shows current blood level, capacity, and tier
 * - Triggers tier recalculation
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
        if (blockEntity instanceof TileAltar altar) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            // Trigger tier recalculation
            altar.checkTier();

            int tierLevel = altar.getTier();

            // Sneak + Right-click to auto-place upgrade blocks
            if (player.isShiftKeyDown() && tierLevel < 6) {
                Map<BlockPos, ResourceLocation> requiredBlocks = AltarUpgradeHelper.getUpgradeBlocks(altar, pos);
                if (requiredBlocks.isEmpty()) {
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.no_upgrade_needed")
                            .withStyle(ChatFormatting.YELLOW),
                        true
                    );
                    return InteractionResult.SUCCESS;
                }

                int placed = 0;
                int missing = 0;
                Map<BlockPos, ResourceLocation> remainingBlocks = new HashMap<>();

                for (Map.Entry<BlockPos, ResourceLocation> entry : requiredBlocks.entrySet()) {
                    BlockPos targetPos = entry.getKey();
                    ResourceLocation blockId = entry.getValue();

                    // Check if position is already occupied
                    if (!level.getBlockState(targetPos).isAir()) {
                        continue; // Skip already filled positions
                    }

                    // Get the block
                    net.minecraft.world.level.block.Block block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(blockId);
                    if (block == null) {
                        continue;
                    }

                    boolean canPlace = false;

                    // Creative mode: just place without consuming
                    if (player.isCreative()) {
                        canPlace = true;
                    } else {
                        // Survival mode: check inventory and consume item
                        net.minecraft.world.item.Item blockItem = block.asItem();
                        if (blockItem != null && player.getInventory().contains(new ItemStack(blockItem))) {
                            // Remove one item from inventory
                            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                ItemStack slotStack = player.getInventory().getItem(i);
                                if (!slotStack.isEmpty() && slotStack.is(blockItem)) {
                                    slotStack.shrink(1);
                                    canPlace = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (canPlace) {
                        level.setBlock(targetPos, block.defaultBlockState(), 3);
                        placed++;
                    } else {
                        remainingBlocks.put(targetPos, blockId);
                        missing++;
                    }
                }

                // Re-check tier after placement
                altar.checkTier();

                // Provide feedback
                if (placed > 0) {
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.placed_blocks", placed, (placed != 1 ? "s" : ""), (tierLevel + 1))
                            .withStyle(ChatFormatting.GREEN),
                        true
                    );

                    // Play placement sound
                    level.playSound(
                        null,
                        pos,
                        SoundEvents.STONE_PLACE,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                    );
                }

                if (missing > 0 && !player.isCreative()) {
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.missing_blocks", missing, (missing != 1 ? "s" : ""))
                            .withStyle(ChatFormatting.YELLOW),
                        true
                    );

                    // Send updated ghost blocks for remaining positions
                    if (player instanceof ServerPlayer serverPlayer && !remainingBlocks.isEmpty()) {
                        AltarGhostBlocksPacket packet = new AltarGhostBlocksPacket(remainingBlocks, 200);
                        AnimusNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
                    }
                }

                if (placed == 0 && !player.isCreative()) {
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.no_blocks_found")
                            .withStyle(ChatFormatting.RED),
                        true
                    );
                }

                return InteractionResult.SUCCESS;
            }

            // Normal right-click: Show information and ghost blocks
            // Get altar information
            int currentBlood = altar.getCurrentBlood();
            int capacity = altar.getCapacity();

            // Display information to player
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.DIVINER_ALTAR_INFO), false
            );
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.DIVINER_BLOOD_INFO, currentBlood, capacity), false
            );

            // Show tier information
            player.displayClientMessage(
                Component.translatable(Constants.Localizations.Text.DIVINER_TIER_INFO, tierLevel), false
            );

            // Send ghost blocks for next tier upgrade (if not max tier)
            if (tierLevel < 6 && player instanceof ServerPlayer serverPlayer) {
                Map<BlockPos, ResourceLocation> ghostBlocks = AltarUpgradeHelper.getUpgradeBlocks(altar, pos);
                if (!ghostBlocks.isEmpty()) {
                    AltarGhostBlocksPacket packet = new AltarGhostBlocksPacket(ghostBlocks, 200); // 10 seconds (200 ticks)
                    AnimusNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);

                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.showing_upgrade", (tierLevel + 1))
                            .withStyle(ChatFormatting.AQUA),
                        true
                    );
                    player.displayClientMessage(
                        Component.translatable("text.component.animus.diviner.sneak_to_autoplace")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                        true
                    );
                }
            } else if (tierLevel >= 6) {
                player.displayClientMessage(
                    Component.translatable("text.component.animus.diviner.max_tier")
                        .withStyle(ChatFormatting.GOLD),
                    true
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

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_FIRST));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_SECOND));
        tooltip.add(Component.translatable(Constants.Localizations.Tooltips.DIVINER_THIRD));
        tooltip.add(Component.translatable("tooltip.animus.diviner.altar_ghost").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.animus.diviner.altar_autoplace").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.animus.diviner.ritual_info").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.animus.diviner.ritual_dismantle").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
