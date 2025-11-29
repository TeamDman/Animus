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
 * - Shift+Right-click ritual to dismantle it
 */
public class ItemSanguineDiviner extends Item {

    public ItemSanguineDiviner() {
        super(new Item.Properties());
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

            // Shift+Right-click to dismantle ritual
            if (player.isShiftKeyDown()) {
                if (ritual != null) {
                    // Stop the ritual
                    ritualStone.stopRitual(Ritual.BreakType.DEACTIVATE);

                    player.displayClientMessage(
                        Component.literal("Ritual dismantled!")
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
                        Component.literal("No ritual to dismantle")
                            .withStyle(ChatFormatting.RED),
                        true
                    );
                }
                return InteractionResult.SUCCESS;
            }

            // Normal click - show ritual information
            if (ritual == null) {
                player.displayClientMessage(
                    Component.literal("No ritual set")
                        .withStyle(ChatFormatting.GRAY),
                    false
                );
            } else {
                String ritualName = ritual.getTranslationKey();
                player.displayClientMessage(
                    Component.literal("Ritual: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.translatable(ritualName).withStyle(ChatFormatting.WHITE)),
                    false
                );
                player.displayClientMessage(
                    Component.literal("Status: ").withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(isActive ? "Active" : "Inactive")
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
                        Component.literal("Owner: ").withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(ownerName)
                                .withStyle(ChatFormatting.YELLOW)),
                        false
                    );
                }

                player.displayClientMessage(
                    Component.literal("Shift+Right-click to dismantle")
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
            int tierLevel = altar.getTier();
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
                        Component.literal("Showing upgrade requirements for Tier " + (tierLevel + 1))
                            .withStyle(ChatFormatting.AQUA),
                        true
                    );
                }
            } else if (tierLevel >= 6) {
                player.displayClientMessage(
                    Component.literal("Altar is already at maximum tier!")
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
        tooltip.add(Component.literal("Right-click ritual to show info").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("Shift+Right-click ritual to dismantle").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
