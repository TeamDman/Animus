package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import java.util.function.Consumer;

/**
 * Ritual of Entropy - Converts items to cobblestone
 * Takes items from chest and converts them to 1 cobblestone per item
 * Activation Cost: 1000 EV
 * Refresh Cost: 1 EV
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
        IAnima network = AnimusRitualHelper.getOwnerNetwork(masterRitualStone);
        if (network == null) {
            return;
        }

        int currentEV = network.getCurrentEV();
        BlockPos masterPos = masterRitualStone.getMasterBlockPos();

        if (level.isClientSide()) {
            return;
        }

        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);

        IItemHandler handler = AnimusRitualHelper.getItemHandler(level, chestPos);
        if (handler == null) {
            return;
        }

        if (currentEV < getRefreshCost()) {
            return;
        }

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.is(Items.COBBLESTONE)) {
                continue;
            }

            handler.extractItem(slot, 1, false);
            ItemHandlerHelper.insertItemStacked(handler, new ItemStack(Items.COBBLESTONE, 1), false);

            AnimaTicket ticket = AnimaTicket.create(getRefreshCost());
            network.syphon(ticket);

            return;
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
        addCornerRunes(components, 1, 0, EnumRuneType.EARTH);
        addParallelRunes(components, 1, 0, EnumRuneType.EARTH);
        addCornerRunes(components, 2, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualEntropy();
    }
}
