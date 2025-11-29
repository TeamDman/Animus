package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.function.Consumer;

/**
 * Ritual of Reparare - Repairs items in an inventory above the ritual
 * Looks for a chest/inventory above the master ritual stone and repairs damaged items within
 * Activation Cost: 5000 LP
 * Refresh Cost: Configurable (default: 50 LP per damage point repaired)
 * Refresh Time: Configurable (default: 100 ticks / 5 seconds)
 * Repair Amount: Configurable (default: 1 damage per item per interval)
 */
@RitualRegister(Constants.Rituals.REPARARE)
public class RitualReparare extends Ritual {
    public static final String CHEST_RANGE = "chest";

    public RitualReparare() {
        super(Constants.Rituals.REPARARE, 0, 5000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.REPARARE);

        // Look for chest directly above the master ritual stone
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        if (network == null) {
            return;
        }

        // Get chest above ritual
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

        // Get config values
        int repairAmount = AnimusConfig.rituals.reparareRitualRepairAmount.get();
        int lpPerDamage = AnimusConfig.rituals.reparareRitualLPPerDamage.get();

        // Track total LP cost for this cycle
        int totalLPCost = 0;
        int itemsRepaired = 0;

        // Iterate through all slots and repair damaged items
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);

            // Skip empty stacks
            if (stack.isEmpty()) {
                continue;
            }

            // Skip non-damageable items
            if (!stack.isDamageableItem()) {
                continue;
            }

            // Skip items with no damage
            if (!stack.isDamaged()) {
                continue;
            }

            // Skip blacklisted items
            if (stack.is(Constants.Tags.DISALLOW_REPAIR)) {
                continue;
            }

            // Calculate how much to repair
            int currentDamage = stack.getDamageValue();
            int actualRepairAmount = Math.min(repairAmount, currentDamage);

            // Calculate LP cost for this repair
            int lpCost = actualRepairAmount * lpPerDamage;

            // Check if we have enough LP
            if (network.getCurrentEssence() < totalLPCost + lpCost) {
                // Not enough LP for this item, skip it
                continue;
            }

            // Repair the item
            stack.setDamageValue(currentDamage - actualRepairAmount);

            // Add to total LP cost
            totalLPCost += lpCost;
            itemsRepaired++;
        }

        // If we repaired anything, consume the LP
        if (totalLPCost > 0 && itemsRepaired > 0) {
            SoulTicket ticket = new SoulTicket(
                Component.translatable(Constants.Localizations.Text.TICKET_REPARARE),
                totalLPCost
            );
            network.syphon(ticket, false);
        }
    }

    @Override
    public int getRefreshCost() {
        // The actual cost is calculated dynamically based on items repaired
        // Return 0 here since we handle LP consumption in performRitual
        return 0;
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.reparareRitualInterval.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        // Create a cross pattern with water runes
        addRune(components, 0, 0, -1, EnumRuneType.WATER);
        addRune(components, 0, 0, 1, EnumRuneType.WATER);
        addRune(components, -1, 0, 0, EnumRuneType.WATER);
        addRune(components, 1, 0, 0, EnumRuneType.WATER);

        // Corner runes for stability
        addRune(components, -1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.EARTH);
        addRune(components, 1, 0, -1, EnumRuneType.EARTH);
        addRune(components, 1, 0, 1, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualReparare();
    }
}
