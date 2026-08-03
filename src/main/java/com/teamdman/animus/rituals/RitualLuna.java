package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.function.Consumer;

/**
 * Ritual of Luna - Harvests light-emitting blocks
 * Scans the effect range for blocks with light level > 0, harvests them, and stores in chest above ritual
 * Uses center-outward search algorithm to prioritize nearby positions
 * Activation Cost: 1000 LP
 * Refresh Cost: Configurable (default: 1 LP)
 * Refresh Time: Configurable (default: 5 ticks)
 */
@RitualRegister(Constants.Rituals.LUNA)
public class RitualLuna extends Ritual {
    private static final Logger LOGGER = LoggerFactory.getLogger(RitualLuna.class);

    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";

    private static final RitualAreaScanner SCANNER = new RitualAreaScanner();

    public RitualLuna() {
        super(Constants.Rituals.LUNA, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.LUNA);

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        int currentEssence = network.getCurrentEssence();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }
        // Check if ritual is enabled
        if (!AnimusConfig.rituals.lunaEnabled.get()) {
            return;
        }


        // Check if player has enough LP
        if (currentEssence < getRefreshCost()) {
            network.causeNausea();
            return;
        }

        // Get chest position
        AreaDescriptor chestRange = mrs.getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        // Find a light-emitting block using center-outward search
        AreaDescriptor effectRange = mrs.getBlockRange(EFFECT_RANGE);
        BlockPos lightPos = findLightEmittingBlock(level, masterPos, effectRange);

        if (lightPos == null) {
            return;
        }

        BlockState state = level.getBlockState(lightPos);
        Block block = state.getBlock();

        // Get the item stack for this block
        ItemStack stack = block.getCloneItemStack(level, lightPos, state);

        // Try to place item in chest if we have one
        boolean shouldRemoveBlock = true;
        if (!stack.isEmpty()) {
            // Try to insert into chest
            if (chestTile != null) {
                IItemHandler handler = chestTile.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
                if (handler != null) {
                    // Try to insert, if successful then we can proceed
                    ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack, true);
                    if (remainder.isEmpty()) {
                        ItemHandlerHelper.insertItem(handler, stack, false);
                    } else {
                        // Chest is full, don't remove block
                        shouldRemoveBlock = false;
                    }
                } else {
                    // No handler, don't remove block
                    shouldRemoveBlock = false;
                }
            } else {
                // No chest, drop at master ritual stone
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    masterPos.getX() + 0.5,
                    masterPos.getY() + 1,
                    masterPos.getZ() + 0.5,
                    stack
                );
                level.addFreshEntity(itemEntity);
            }
        }

        // Remove block and consume LP (even if no valid item dropped)
        if (shouldRemoveBlock) {
            level.removeBlock(lightPos, false);

            // Consume LP
            SoulTicket ticket = new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_LUNA),
                getRefreshCost()
            );
            network.syphon(ticket, false);
        }
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.lunaRefreshCost.get();
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.lunaRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        for (int layer = 0; layer < 3; layer++) {
            addRune(components, 2, layer, 2, EnumRuneType.EARTH);
            addRune(components, -2, layer, 2, EnumRuneType.EARTH);
            addRune(components, 2, layer, -2, EnumRuneType.EARTH);
            addRune(components, -2, layer, -2, EnumRuneType.EARTH);
        }
    }

    /**
     * Find a light-emitting block using center-outward search
     * Expands outward in square rings across the ritual's effect range (modifiable via Ritual Tinkerer)
     */
    private BlockPos findLightEmittingBlock(Level level, BlockPos masterPos, AreaDescriptor effectRange) {
        return SCANNER.scan(level, masterPos, effectRange, checkPos ->
            level.getBlockState(checkPos).getLightEmission() > 0);
    }

    @Override
    public void stopRitual(IMasterRitualStone mrs, BreakType breakType) {
        SCANNER.forget(mrs.getWorldObj(), mrs.getMasterBlockPos());
        super.stopRitual(mrs, breakType);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualLuna();
    }
}
