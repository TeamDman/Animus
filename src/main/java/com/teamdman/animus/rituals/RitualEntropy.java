package com.teamdman.animus.rituals;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
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

import java.util.*;
import java.util.function.Consumer;

/**
 * Ritual of Entropy - Converts items to cobblestone value
 * Analyzes crafting recipes to determine cobblestone value of items
 * Activation Cost: 1000 LP
 * Refresh Cost: 1 LP
 * Refresh Time: 1 tick
 */
@RitualRegister(Constants.Rituals.ENTROPY)
public class RitualEntropy extends Ritual {
    public static final String CHEST_RANGE = "chest";
    final HashMap<Item, Integer> indexed = new HashMap<>();

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

        // Process items in chest
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            // Skip cobblestone itself
            if (stack.is(Items.COBBLESTONE)) {
                continue;
            }

            // Calculate cobblestone value
            int cobbleValue = getCobbleValue(new ArrayList<>(), stack, 0);
            if (cobbleValue > 0) {
                // Extract one item
                handler.extractItem(slot, 1, false);

                // Insert cobblestone
                while (cobbleValue > 0) {
                    int stackSize = Math.min(cobbleValue, 64);
                    ItemHandlerHelper.insertItemStacked(handler, new ItemStack(Items.COBBLESTONE, stackSize), false);
                    cobbleValue -= stackSize;
                }

                // Consume LP
                SoulTicket ticket = new SoulTicket(
                    Component.translatable(Constants.Localizations.Text.TICKET_ENTROPY),
                    getRefreshCost()
                );
                network.syphon(ticket, false);

                return; // Only process one item per tick
            }
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

    /**
     * Calculate the cobblestone value of an item by analyzing crafting recipes
     * TODO: Implement full recipe tree walking for 1.20.1
     * Currently returns a simple heuristic value
     */
    public int getCobbleValue(List<Item> fetchList, ItemStack input, int layer) {
        if (input.isEmpty()) {
            return 0;
        }

        // Check cache
        if (indexed.containsKey(input.getItem())) {
            return indexed.get(input.getItem());
        }

        // Prevent infinite recursion
        if (fetchList.contains(input.getItem())) {
            return 1;
        }

        // Limit recursion depth
        if (layer > 8) {
            return 0;
        }

        // TODO: Implement recipe tree walking for 1.20.1
        // The recipe system has changed significantly in 1.20.1
        // For now, return a simple heuristic based on item properties
        int value = getSimpleHeuristicValue(input);

        indexed.put(input.getItem(), value);
        return value;
    }

    /**
     * Simple heuristic for item value when recipe walking is not implemented
     */
    private int getSimpleHeuristicValue(ItemStack stack) {
        // Basic heuristic - can be improved
        if (stack.is(Items.DIRT) || stack.is(Items.SAND) || stack.is(Items.GRAVEL)) {
            return 1;
        } else if (stack.is(Items.STONE) || stack.is(Items.COBBLED_DEEPSLATE)) {
            return 1;
        } else if (stack.is(Items.IRON_INGOT)) {
            return 4;
        } else if (stack.is(Items.GOLD_INGOT)) {
            return 8;
        } else if (stack.is(Items.DIAMOND)) {
            return 16;
        }
        // Default value for unknown items
        return 2;
    }
}
