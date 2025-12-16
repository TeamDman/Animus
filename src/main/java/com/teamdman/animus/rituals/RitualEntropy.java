package com.teamdman.animus.rituals;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.api.soul.SoulTicket;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import java.util.function.Consumer;

/**
 * Ritual of Entropy - Converts items to cobblestone
 * Takes items from chest and converts them to 1 cobblestone per item
 * Activation Cost: 1000 LP
 * Refresh Cost: 1 LP
 * Refresh Time: 1 tick
 */
public class RitualEntropy extends Ritual {
    public static final String CHEST_RANGE = "chest";

    public RitualEntropy() {
        super(Constants.Rituals.ENTROPY, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ENTROPY);

        addBlockRange(CHEST_RANGE, RitualAreaDescriptors.singleBlockAbove());
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
        if (network == null) {
            return;
        }

        int currentEssence = network.getCurrentEssence();
        BlockPos masterPos = masterRitualStone.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        // Get chest position
        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);

        // Get item handler using NeoForge capability system
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, null);
        if (handler == null) {
            return;
        }

        if (currentEssence < getRefreshCost()) {
            // Note: causeNausea removed in BM 4.0
            return;
        }

        // Process items in chest - convert to 1 cobblestone each
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            // Skip cobblestone itself
            if (stack.is(Items.COBBLESTONE)) {
                continue;
            }

            // Extract one item and give one cobblestone
            handler.extractItem(slot, 1, false);
            ItemHandlerHelper.insertItemStacked(handler, new ItemStack(Items.COBBLESTONE, 1), false);

            // Consume LP
            SoulTicket ticket = SoulTicket.create(getRefreshCost());
            network.syphon(ticket);

            return; // Only process one item per tick
        }
    }

    @Override
    public int getRefreshCost() {
        return 1;
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Inner ring (3x3 minus center)
        addCornerRunes(components, 1, 0, EnumRuneType.EARTH);
        addParallelRunes(components, 1, 0, EnumRuneType.EARTH);

        // Outer corners
        addCornerRunes(components, 2, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualEntropy();
    }
}
