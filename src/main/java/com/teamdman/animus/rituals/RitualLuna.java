package com.teamdman.animus.rituals;

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
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.function.Consumer;

/**
 * Ritual of Luna - Harvests light-emitting blocks
 * Scans the effect range for blocks with light level > 0, harvests them, and stores in chest above ritual
 * Activation Cost: 1000 LP
 * Refresh Cost: 1 LP
 * Refresh Time: 5 ticks
 */
@RitualRegister(Constants.Rituals.LUNA)
public class RitualLuna extends Ritual {
    public static final String CHEST_RANGE = "chest";
    public static final String EFFECT_RANGE = "effect";

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

        // Check if player has enough LP
        if (currentEssence < getRefreshCost()) {
            network.causeNausea();
            return;
        }

        // Get chest position
        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        // Scan for light-emitting blocks
        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        for (BlockPos pos : effectRange.getContainedPositions(masterPos)) {
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            // Check if block emits light
            if (state.getLightEmission() > 0) {
                // Get the item stack for this block
                ItemStack stack = block.getCloneItemStack(level, pos, state);

                // If we got a valid stack
                if (!stack.isEmpty()) {
                    // Try to insert into chest
                    if (chestTile != null) {
                        IItemHandler handler = chestTile.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
                        if (handler != null) {
                            // Try to insert, if successful then remove block
                            ItemStack remainder = ItemHandlerHelper.insertItem(handler, stack, true);
                            if (remainder.isEmpty()) {
                                ItemHandlerHelper.insertItem(handler, stack, false);
                                level.removeBlock(pos, false);

                                // Consume LP
                                SoulTicket ticket = new SoulTicket(
                                    Component.translatable(Constants.Localizations.Text.TICKET_LUNA),
                                    getRefreshCost()
                                );
                                network.syphon(ticket, false);

                                return; // Only harvest one block per tick
                            }
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
                        level.removeBlock(pos, false);

                        // Consume LP
                        SoulTicket ticket = new SoulTicket(
                            Component.translatable(Constants.Localizations.Text.TICKET_LUNA),
                            getRefreshCost()
                        );
                        network.syphon(ticket, false);

                        return;
                    }
                }
            }
        }
    }

    @Override
    public int getRefreshCost() {
        return 1;
    }

    @Override
    public int getRefreshTime() {
        return 5;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        for (int layer = 0; layer < 3; layer++) {
            addRune(components, 2, layer, 2, EnumRuneType.DUSK);
            addRune(components, -2, layer, 2, EnumRuneType.DUSK);
            addRune(components, 2, layer, -2, EnumRuneType.DUSK);
            addRune(components, -2, layer, -2, EnumRuneType.DUSK);
        }
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualLuna();
    }
}
