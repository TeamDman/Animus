package com.teamdman.animus.rituals;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.types.RitualType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.function.Consumer;

/**
 * Ritual of Entropy - Converts items to cobblestone
 * Takes items from chest and converts them to 1 cobblestone per item
 * Activation Cost: 1000 LP
 * Refresh Cost: 1 LP
 * Refresh Time: 1 tick
 */
@RitualRegister(Constants.Rituals.ENTROPY)
public class RitualEntropy extends Ritual {
    public static final String CHEST_RANGE = "chest";

    public RitualEntropy() {
        super(new RitualType(Constants.Rituals.ENTROPY, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ENTROPY));

        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
        if (network == null) {
            return;
        }

        int currentEssence = network.getCurrentEssence();
        BlockPos masterPos = masterRitualStone.getBlockPos();

        if (level.isClientSide) {
            return;
        }

        // Get chest
        AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);
        BlockEntity chestTile = level.getBlockEntity(chestPos);

        if (chestTile == null) {
            return;
        }

        // Get item handler from chest
        IItemHandler handler = chestTile.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler == null) {
            return;
        }

        if (currentEssence < getRefreshCost()) {
            network.causeNausea();
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
            SoulTicket ticket = new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_ENTROPY),
                getRefreshCost()
            );
            network.syphon(ticket, false);

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
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                addRune(components, x, 0, z, RitualType.EnumRuneType.EARTH);
            }
        }
        addRune(components, -2, 0, -2, RitualType.EnumRuneType.EARTH);
        addRune(components, -2, 0, 2, RitualType.EnumRuneType.EARTH);
        addRune(components, 2, 0, -2, RitualType.EnumRuneType.EARTH);
        addRune(components, 2, 0, 2, RitualType.EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualEntropy();
    }
}
